package app.notekea.notes.data

import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.notes.domain.IndexError
import app.notekea.notes.domain.NoteChunk
import app.notekea.notes.domain.RelativePath
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.store.FSDirectory
import java.nio.file.Files
import kotlin.io.path.listDirectoryEntries
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The search index on real files (`ROADMAP.md` 2.3), with vectors made by hand: found by meaning and by words (accents
 * and Spanish plurals aside), replaced, removed and cleared, kept on disk, one per vault.
 */
class LuceneVectorIndexTest {
    private val testDispatcher = StandardTestDispatcher()
    private val root = Files.createTempDirectory("notekea-index")
    private fun index() = LuceneVectorIndex(
        indexRoot = root.toString(),
        dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
    )

    @Test
    fun `a question finds the chunk closest in meaning, and by its words without accents or plurals`() = runTest(
        context = testDispatcher
    ) {
        val index = index()
        index.put(
            vaultPath = VAULT,
            note = AREPAS,
            chunks = listOf(chunk(note = AREPAS, text = "Masa de maíz")),
            vectors = listOf(EAST)
        )
        index.put(
            vaultPath = VAULT,
            note = SONG,
            chunks = listOf(chunk(note = SONG, text = "Una canción y sus recetas")),
            vectors = listOf(NORTH)
        )

        val byMeaning = notesFound(index = index, text = "", vector = NORTH_EAST_ISH)
        val byWords = notesFound(index = index, text = "cancion receta", vector = EAST)

        assertEquals(expected = listOf(listOf(SONG, AREPAS), listOf(SONG, AREPAS)), actual = listOf(byMeaning, byWords))
    }

    @Test
    fun `the nearest chunks by meaning alone come closest first`() = runTest(context = testDispatcher) {
        val index = index()
        index.put(vaultPath = VAULT, note = AREPAS, chunks = listOf(chunk(AREPAS, "masa")), vectors = listOf(EAST))
        index.put(vaultPath = VAULT, note = SONG, chunks = listOf(chunk(SONG, "song")), vectors = listOf(NORTH))

        val hits = (index.nearest(vaultPath = VAULT, vector = NORTH_EAST_ISH, limit = 2) as Outcome.Success).value

        assertEquals(expected = listOf(SONG, AREPAS), actual = hits.map { it.note })
    }

    // Audit: a question of over 1,024 different words threw out of the search instead of answering.
    @Test
    fun `a very long question is answered from its first words`() = runTest(context = testDispatcher) {
        val index = index()
        index.put(vaultPath = VAULT, note = SONG, chunks = listOf(chunk(SONG, "palabra1")), vectors = listOf(NORTH))
        val question = (1..2_000).joinToString(separator = " ") { "palabra$it" }

        val found = index.search(vaultPath = VAULT, text = question, vector = NORTH, limit = 1)

        assertEquals(expected = listOf(SONG), actual = (found as Outcome.Success).value.map { it.note })
    }

    // Audit: opening the index was outside the guard, so a folder that could not be read threw out of the search.
    @Test
    fun `an index folder that cannot be read is told, not thrown`() = runTest(context = testDispatcher) {
        val index = index()
        index.put(vaultPath = VAULT, note = SONG, chunks = listOf(chunk(SONG, "song")), vectors = listOf(NORTH))
        val folder = root.listDirectoryEntries().single()
        folder.toFile().deleteRecursively()
        Files.createFile(folder)

        assertEquals(
            expected = Outcome.Failure(error = IndexError.Unavailable),
            actual = index.nearest(vaultPath = VAULT, vector = NORTH, limit = 1)
        )
    }

    @Test
    fun `a note put again replaces its chunks, a removed one is gone, and clearing empties the vault`() = runTest(
        context = testDispatcher
    ) {
        val index = index()
        index.put(
            vaultPath = VAULT,
            note = AREPAS,
            chunks = listOf(chunk(AREPAS, "one"), chunk(AREPAS, "two", 1)),
            vectors = listOf(EAST, EAST)
        )
        index.put(vaultPath = VAULT, note = AREPAS, chunks = listOf(chunk(AREPAS, "only")), vectors = listOf(EAST))
        index.put(vaultPath = VAULT, note = SONG, chunks = listOf(chunk(SONG, "song")), vectors = listOf(NORTH))
        val afterReplace = notesFound(index = index, text = "", vector = EAST)
        index.remove(vaultPath = VAULT, note = SONG)
        val afterRemove = notesFound(index = index, text = "", vector = EAST)
        index.clear(vaultPath = VAULT)

        assertEquals(
            expected = listOf(listOf(AREPAS, SONG), listOf(AREPAS), emptyList()),
            actual = listOf(afterReplace, afterRemove, notesFound(index = index, text = "", vector = EAST))
        )
    }

    @Test
    fun `what is put stays on disk for the next run, one index per vault`() = runTest(context = testDispatcher) {
        index().put(vaultPath = VAULT, note = AREPAS, chunks = listOf(chunk(AREPAS, "masa")), vectors = listOf(EAST))
        index().put(vaultPath = OTHER_VAULT, note = SONG, chunks = listOf(chunk(SONG, "song")), vectors = listOf(NORTH))

        assertEquals(
            expected = listOf(listOf(AREPAS), listOf(SONG)),
            actual = listOf(notesFound(index(), "", EAST, VAULT), notesFound(index(), "", EAST, OTHER_VAULT))
        )
    }

    @Test
    fun `an index another Notekea is writing makes a change wait, and an empty one finds nothing`() = runTest(
        context = testDispatcher
    ) {
        val index = index()
        val empty = notesFound(index = index, text = "masa", vector = EAST)
        index.put(vaultPath = VAULT, note = AREPAS, chunks = listOf(chunk(AREPAS, "masa")), vectors = listOf(EAST))
        val folder = root.listDirectoryEntries().single()

        val busy = FSDirectory.open(folder).use { directory ->
            IndexWriter(directory, IndexWriterConfig()).use { index.remove(vaultPath = VAULT, note = AREPAS) }
        }

        assertEquals(
            expected = emptyList<RelativePath>() to Outcome.Failure(error = IndexError.Busy),
            actual =
            empty to busy
        )
    }

    private suspend fun notesFound(
        index: LuceneVectorIndex,
        text: String,
        vector: FloatArray,
        vaultPath: String = VAULT
    ): List<RelativePath> =
        (index.search(vaultPath = vaultPath, text = text, vector = vector, limit = 10) as Outcome.Success).value.map {
            it.note
        }

    private companion object {
        const val VAULT = "/home/ana/Notes"
        const val OTHER_VAULT = "/home/ana/Work"
        val AREPAS = RelativePath(value = "Recipes/Arepas.md")
        val SONG = RelativePath(value = "Music/Song.md")
        val EAST = floatArrayOf(1f, 0f, 0f, 0f)
        val NORTH = floatArrayOf(0f, 1f, 0f, 0f)
        val NORTH_EAST_ISH = floatArrayOf(0.6f, 0.8f, 0f, 0f)

        fun chunk(note: RelativePath, text: String, index: Int = 0) = NoteChunk(
            note = note,
            index = index,
            headingPath = listOf("Title"),
            range = 0 until text.length,
            text = text,
            tokens = 1
        )
    }
}
