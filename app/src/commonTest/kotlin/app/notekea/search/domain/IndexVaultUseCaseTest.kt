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

/**
 * Bringing a vault's search index up to date: every note the first time, then only what changed; gone notes leave, a
 * stopped run resumes with the notes left, and a failing model or a busy index stops the run.
 */
class IndexVaultUseCaseTest : IndexVaultTestBase() {
    @Test
    fun `the first run indexes every note, telling its progress after each one`() = runTest {
        val notes = write(AREPAS to "# Arepas\nCorn dough.", DUNE to "# Dune\nDesert and water.")

        val progress = indexVault(
            vaultPath = VAULT,
            notes = notes
        ).toList()

        assertEquals(
            expected = listOf(0, 1, 2, 2) to IndexProgress(
                total = 2,
                done = 2,
                indexed = 2,
                isFinished = true
            ),
            actual = progress.map { it.done } to progress.last()
        )

        assertEquals(
            expected = setOf(AREPAS, DUNE),
            actual = index
                .notes
                .getValue(VAULT)
                .keys
        )

        assertEquals(
            expected = listOf("Dune › Dune\n# Dune\nDesert and water."),
            actual = embedder.embedded.take(n = 1)
        )
    }

    @Test
    fun `the next run indexes only the note whose text changed, and a touched note keeps its vectors`() =
        runTest {
            val first =
                write(AREPAS to "# Arepas\nCorn dough.", DUNE to "# Dune\nDesert and water.")

            indexVault(
                vaultPath = VAULT,
                notes = first
            ).last()

            index.puts.clear()

            val changed = write(AREPAS to "# Arepas\nCorn dough and salt.", size = 2) +
                touched(file = first.last())

            val last = indexVault(
                vaultPath = VAULT,
                notes = changed
            ).last()

            assertEquals(
                expected = listOf(AREPAS),
                actual = index.puts
            )

            assertEquals(
                expected = IndexProgress(
                    total = 2,
                    done = 2,
                    indexed = 1,
                    isFinished = true
                ),
                actual = last
            )
        }

    @Test
    fun `a note gone from the vault leaves the index and its state`() = runTest {
        indexVault(
            vaultPath = VAULT,
            notes = write(AREPAS to "Corn.", DUNE to "Water.")
        ).last()

        indexVault(
            vaultPath = VAULT,
            notes = write(DUNE to "Water.")
        ).last()

        assertEquals(
            expected = setOf(DUNE) to setOf(DUNE),
            actual = index
                .notes
                .getValue(VAULT)
                .keys to states
                .states
                .getValue(VAULT)
                .keys
        )
    }

    // A run stopped by closing Notekea goes on with the notes left, counted from those already indexed: from zero it
    // would look as if indexing started over.
    @Test
    fun `a run stopped after its first note goes on with the notes left, counted from those done`() =
        runTest {
            val notes = write(AREPAS to "Corn.", DUNE to "Water.", LISBON to "River.")

            indexVault(
                vaultPath = VAULT,
                notes = notes
            ).first {
                it.done == 1
            }

            index.puts.clear()

            val resumed = indexVault(
                vaultPath = VAULT,
                notes = notes
            ).toList()

            assertEquals(
                expected = listOf(AREPAS, LISBON),
                actual = index.puts
            )

            assertEquals(
                expected = (1 to 3) to IndexProgress(
                    total = 3,
                    done = 3,
                    indexed = 2,
                    isFinished = true
                ),
                actual = (resumed.first().done to resumed.first().total) to resumed.last()
            )
        }

    @Test
    fun `a failing model or a busy index stops the run, and nothing is marked done`() = runTest {
        val notes = write(AREPAS to "Corn.")
        embedder.failure = EmbeddingError.EngineFailed

        val model = indexVault(
            vaultPath = VAULT,
            notes = notes
        ).last()

        embedder.failure = null
        index.isBusy = true

        val busy = indexVault(
            vaultPath = VAULT,
            notes = notes
        ).last()

        assertEquals(
            expected = listOf(IndexRunError.Model, IndexRunError.Index) to emptyList(),
            actual = listOf(model.error, busy.error) to states.read(vaultPath = VAULT)
        )
    }

    @Test
    fun `another model indexes every note again`() = runTest {
        val notes = write(AREPAS to "Corn.", DUNE to "Water.")

        indexVault(
            vaultPath = VAULT,
            notes = notes
        ).last()

        val last = indexVault(embedder = FakeTextEmbedder(modelId = "another-model"))(
            vaultPath = VAULT,
            notes = notes
        ).last()

        assertEquals(
            expected = 2,
            actual = last.indexed
        )
    }
}
