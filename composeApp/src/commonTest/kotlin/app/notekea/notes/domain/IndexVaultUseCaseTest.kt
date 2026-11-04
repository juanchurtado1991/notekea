package app.notekea.notes.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.embedding.domain.EmbeddingError
import app.notekea.core.embedding.domain.FakeTextEmbedder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

/**
 * Bringing a vault's search index up to date (`ROADMAP.md` 2.3): every note the first time, then only what changed;
 * gone notes leave; a stopped run resumes with the notes left; what cannot be indexed is skipped or stops the run.
 */
class IndexVaultUseCaseTest {
    private val contents = FakeNoteContentStore()
    private val embedder = FakeTextEmbedder()
    private val index = FakeVectorIndex()
    private val states = FakeIndexStateStore()
    private val indexVault = indexVault(embedder = embedder)

    private fun indexVault(
        embedder: FakeTextEmbedder,
        vectorIndex: VectorIndex = index
    ) = IndexVaultUseCase(
        planIndex = PlanIndexUseCase(),
        chunkNote = ChunkNoteUseCase(noteSections = NoteSections(outlineFinder = OutlineFinder())),
        noteContentStore = contents,
        textEmbedder = embedder,
        vectorIndex = vectorIndex,
        indexStateStore = states
    )

    @Test
    fun `the first run indexes every note, telling its progress after each one`() = runTest {
        val notes = write(AREPAS to "# Arepas\nCorn dough.", DUNE to "# Dune\nDesert and water.")

        val progress = indexVault(vaultPath = VAULT, notes = notes).toList()

        assertEquals(
            expected = listOf(0, 1, 2, 2) to IndexProgress(total = 2, done = 2, indexed = 2, isFinished = true),
            actual = progress.map { it.done } to progress.last()
        )
        assertEquals(expected = setOf(AREPAS, DUNE), actual = index.notes.getValue(VAULT).keys)
        assertEquals(
            expected = listOf("Dune › Dune\n# Dune\nDesert and water."),
            actual = embedder.embedded.take(n = 1)
        )
    }

    @Test
    fun `the next run indexes only the note whose text changed, and a touched note keeps its vectors`() = runTest {
        val first = write(AREPAS to "# Arepas\nCorn dough.", DUNE to "# Dune\nDesert and water.")
        indexVault(vaultPath = VAULT, notes = first).last()
        index.puts.clear()
        val changed = write(AREPAS to "# Arepas\nCorn dough and salt.", size = 2) + touched(file = first.last())

        val last = indexVault(vaultPath = VAULT, notes = changed).last()

        assertEquals(expected = listOf(AREPAS), actual = index.puts)
        assertEquals(expected = IndexProgress(total = 2, done = 2, indexed = 1, isFinished = true), actual = last)
    }

    @Test
    fun `a note gone from the vault leaves the index and its state`() = runTest {
        indexVault(vaultPath = VAULT, notes = write(AREPAS to "Corn.", DUNE to "Water.")).last()

        indexVault(vaultPath = VAULT, notes = write(DUNE to "Water.")).last()

        assertEquals(
            expected = setOf(DUNE) to setOf(DUNE),
            actual = index.notes.getValue(VAULT).keys to states.states.getValue(VAULT).keys
        )
    }

    // Smoke 2.3 on Linux: a run stopped by closing Notekea went on with the notes left, but counted them from zero, so
    // it looked as if indexing started over. It now goes on from the notes already indexed.
    @Test
    fun `a run stopped after its first note goes on with the notes left, counted from those done`() = runTest {
        val notes = write(AREPAS to "Corn.", DUNE to "Water.", LISBON to "River.")
        indexVault(vaultPath = VAULT, notes = notes).first { it.done == 1 }
        index.puts.clear()

        val resumed = indexVault(vaultPath = VAULT, notes = notes).toList()

        assertEquals(expected = listOf(AREPAS, LISBON), actual = index.puts)
        assertEquals(
            expected = (1 to 3) to IndexProgress(total = 3, done = 3, indexed = 2, isFinished = true),
            actual = (resumed.first().done to resumed.first().total) to resumed.last()
        )
    }

    @Test
    fun `a note too large, unreadable or too long is skipped and named, the others indexed`() = runTest {
        val notes = write(AREPAS to "Corn.", LONG to List(size = 50) { "word" }.joinToString(separator = " ")) +
            NoteFile(path = HUGE, size = IndexVaultUseCase.MAX_NOTE_BYTES + 1, modifiedAt = DATE) +
            NoteFile(path = LOCKED, size = 1, modifiedAt = DATE)

        val shortModel = indexVault(embedder = FakeTextEmbedder(maxTokens = SHORT_LIMIT))
        val last = shortModel(vaultPath = VAULT, notes = notes).last()

        assertEquals(
            expected = listOf(
                SkippedNote(path = HUGE, reason = SkipReason.TOO_LARGE),
                SkippedNote(path = LOCKED, reason = SkipReason.UNREADABLE),
                SkippedNote(path = LONG, reason = SkipReason.TOO_LONG)
            ),
            actual = last.skipped
        )
        assertEquals(expected = setOf(AREPAS), actual = index.notes.getValue(VAULT).keys)
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
                return index.put(vaultPath = vaultPath, note = note, chunks = chunks, vectors = vectors)
            }
        }
        val notes = write(AREPAS to "Corn.", DUNE to "Water.", LISBON to "River.")

        val last = indexVault(embedder = embedder, vectorIndex = broken)(vaultPath = VAULT, notes = notes).last()

        assertEquals(expected = listOf(DUNE to SkipReason.FAILED), actual = last.skipped.map { it.path to it.reason })
        assertEquals(
            expected = true to setOf(AREPAS, LISBON),
            actual =
            last.isFinished to index.notes.getValue(VAULT).keys
        )
    }

    @Test
    fun `a failing model or a busy index stops the run, and nothing is marked done`() = runTest {
        val notes = write(AREPAS to "Corn.")
        embedder.failure = EmbeddingError.EngineFailed
        val model = indexVault(vaultPath = VAULT, notes = notes).last()
        embedder.failure = null
        index.isBusy = true
        val busy = indexVault(vaultPath = VAULT, notes = notes).last()

        assertEquals(
            expected = listOf(IndexRunError.Model, IndexRunError.Index) to emptyList(),
            actual = listOf(model.error, busy.error) to states.read(vaultPath = VAULT)
        )
    }

    @Test
    fun `another model indexes every note again`() = runTest {
        val notes = write(AREPAS to "Corn.", DUNE to "Water.")
        indexVault(vaultPath = VAULT, notes = notes).last()

        val last = indexVault(
            embedder = FakeTextEmbedder(modelId = "another-model")
        )(vaultPath = VAULT, notes = notes).last()

        assertEquals(expected = 2, actual = last.indexed)
    }

    /** Writes the notes and gives their files, of [size] and dated [DATE]. */
    private fun write(
        vararg notes: Pair<RelativePath, String>,
        size: Long = 1
    ): List<NoteFile> = notes.map { (path, text) ->
        contents.texts[path] = text
        NoteFile(path = path, size = size, modifiedAt = DATE)
    }

    /** [file] saved again unchanged: a new date, the same text. */
    private fun touched(file: NoteFile) = file.copy(modifiedAt = Instant.parse(input = "2026-11-03T11:00:00Z"))

    private companion object {
        const val VAULT = "/home/ana/Notes"

        // Words a text may have in the test of a chunk longer than the model reads.
        const val SHORT_LIMIT = 10
        val DATE = Instant.parse(input = "2026-11-03T10:00:00Z")
        val AREPAS = RelativePath(value = "Recipes/Arepas.md")
        val DUNE = RelativePath(value = "Books/Dune.md")
        val LISBON = RelativePath(value = "Trips/Lisbon.md")
        val LONG = RelativePath(value = "Long.md")
        val HUGE = RelativePath(value = "Huge export.md")
        val LOCKED = RelativePath(value = "Locked.md")
    }
}
