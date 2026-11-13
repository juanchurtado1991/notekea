package app.notekea.search.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.embedding.domain.EmbeddingError
import app.notekea.core.embedding.domain.FakeTextEmbedder
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.FakeVectorIndex
import app.notekea.core.notes.domain.NoteFile
import app.notekea.core.notes.domain.NoteSections
import app.notekea.core.notes.domain.OutlineFinder
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.index.ChunkNoteUseCase
import app.notekea.core.notes.domain.index.IndexError
import app.notekea.core.notes.domain.index.IndexRunError
import app.notekea.core.notes.domain.index.NoteChunk
import app.notekea.core.notes.domain.index.VectorIndex
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

/** What a run of the search index cannot index is skipped and named, and the other notes still go in. */
class IndexVaultSkippedNotesTest : IndexVaultTestBase() {
    @Test
    fun `a note too large, unreadable or too long is skipped and named, the others indexed`() =
        runTest {
            val notes = write(
                AREPAS to "Corn.",
                LONG to List(size = 50) { "word" }.joinToString(separator = " ")
            ) +
                NoteFile(
                    path = HUGE,
                    size = IndexVaultUseCase.MAX_NOTE_BYTES + 1,
                    modifiedAt = DATE
                ) +
                NoteFile(
                    path = LOCKED,
                    size = 1,
                    modifiedAt = DATE
                )

            val shortModel = indexVault(embedder = FakeTextEmbedder(maxTokens = SHORT_LIMIT))

            val last = shortModel(
                vaultPath = VAULT,
                notes = notes
            ).last()

            assertEquals(
                expected = listOf(
                    SkippedNote(
                        path = HUGE,
                        reason = SkipReason.TOO_LARGE
                    ),
                    SkippedNote(
                        path = LOCKED,
                        reason = SkipReason.UNREADABLE
                    ),
                    SkippedNote(
                        path = LONG,
                        reason = SkipReason.TOO_LONG
                    )
                ),
                actual = last.skipped
            )

            assertEquals(
                expected = setOf(AREPAS),
                actual = index
                    .notes
                    .getValue(VAULT)
                    .keys
            )
        }

    // Audit: one note that broke the chunker ended every run, so no note after it was ever indexed.
    @Test
    fun `an unexpected failure on one note skips it, the others still indexed`() = runTest {
        val broken = object : VectorIndex by index {
            override suspend fun put(
                vaultPath: String,
                note: RelativePath,
                chunks: List<NoteChunk>,
                vectors: List<FloatArray>
            ): Outcome<Unit, IndexError> {
                check(note != DUNE)

                return index.put(
                    vaultPath = vaultPath,
                    note = note,
                    chunks = chunks,
                    vectors = vectors
                )
            }
        }

        val notes = write(AREPAS to "Corn.", DUNE to "Water.", LISBON to "River.")

        val last = indexVault(
            embedder = embedder,
            vectorIndex = broken
        )(
            vaultPath = VAULT,
            notes = notes
        ).last()

        assertEquals(
            expected = listOf(DUNE to SkipReason.FAILED),
            actual = last.skipped.map {
                it.path to it.reason
            }
        )

        assertEquals(
            expected = true to setOf(AREPAS, LISBON),
            actual =
            last.isFinished to index
                .notes
                .getValue(VAULT)
                .keys
        )
    }

    // Audit: both kept the vectors of their old text, in search results, and were read and embedded again every run.
    @Test
    fun `a note grown too large or too long leaves the index, and is not read again until it changes`() =
        runTest {
            val shortModel = indexVault(embedder = shortEmbedder)
            val small = write(AREPAS to "Corn.", LONG to "Few words.")

            shortModel(
                vaultPath = VAULT,
                notes = small
            ).last()

            val grown = listOf(small[0].copy(size = IndexVaultUseCase.MAX_NOTE_BYTES + 1)) +
                write(LONG to List(size = 50) { "word" }.joinToString(separator = " "), size = 2)

            val first = shortModel(
                vaultPath = VAULT,
                notes = grown
            ).last()

            shortEmbedder.embedded.clear()

            val next = shortModel(
                vaultPath = VAULT,
                notes = grown
            ).last()

            assertEquals(
                expected = emptySet<RelativePath>(),
                actual = index
                    .notes
                    .getValue(VAULT)
                    .keys
            )

            assertEquals(
                expected = first.skipped,
                actual = next.skipped
            )

            assertEquals(
                expected = listOf(SkipReason.TOO_LONG, SkipReason.TOO_LARGE) to emptyList<String>(),
                actual = next.skipped.map { it.reason } to shortEmbedder.embedded
            )
        }

    @Test
    fun `a note back to a size it can be indexed at goes in the index again`() = runTest {
        val notes = write(AREPAS to "Corn.").map {
            it.copy(
                size =
                IndexVaultUseCase.MAX_NOTE_BYTES + 1
            )
        }

        indexVault(
            vaultPath = VAULT,
            notes = notes
        ).last()

        val last = indexVault(
            vaultPath = VAULT,
            notes = write(AREPAS to "Corn.")
        ).last()

        assertEquals(
            expected = 1 to emptyList<SkippedNote>(),
            actual = last.indexed to last.skipped
        )

        assertEquals(
            expected = setOf(AREPAS),
            actual = index
                .notes
                .getValue(VAULT)
                .keys
        )
    }

    // Audit: one chunk the model refused left its whole note out of the index.
    @Test
    fun `a chunk the model refuses is left out, the rest of its note indexed`() = runTest {
        val longSection = List(size = 150) { "word" }.joinToString(separator = " ")
        val notes = write(LONG to "# One\n$longSection\n\n# Two\nShort section.")

        val last = indexVault(embedder = FakeTextEmbedder(maxTokens = 100))(
            vaultPath = VAULT,
            notes = notes
        ).last()

        val held = index
            .notes
            .getValue(VAULT)
            .getValue(LONG)
            .map { (chunk, _) -> chunk.headingPath }

        assertEquals(
            expected = 1 to emptyList<SkippedNote>(),
            actual = last.indexed to last.skipped
        )

        assertEquals(
            expected = listOf(listOf("Two")),
            actual = held
        )
    }

    // Audit: the title and headings in front of a chunk that just fits pushed it past the model's limit.
    @Test
    fun `a long title is cut to what the chunk leaves, so the note is still indexed`() = runTest {
        val title = RelativePath(
            value =
            List(size = 50) { "title" }.joinToString(separator = " ") + ".md"
        )

        val notes = write(title to "Six short words in this note.")

        val last = indexVault(embedder = FakeTextEmbedder(maxTokens = 20))(
            vaultPath = VAULT,
            notes = notes
        ).last()

        assertEquals(
            expected = 1 to emptyList<SkippedNote>(),
            actual = last.indexed to last.skipped
        )
    }
}
