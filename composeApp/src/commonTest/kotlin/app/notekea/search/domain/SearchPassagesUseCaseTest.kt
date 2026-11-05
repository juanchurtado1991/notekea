package app.notekea.search.domain

import app.notekea.core.domain.FakeAppDispatchers
import app.notekea.core.domain.Outcome
import app.notekea.core.embedding.domain.EmbeddingError
import app.notekea.core.embedding.domain.FakeTextEmbedder
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.FakeVectorIndex
import app.notekea.core.notes.domain.IndexRunError
import app.notekea.core.notes.domain.MarkdownLineStripper
import app.notekea.core.notes.domain.NaturalNameComparator
import app.notekea.core.notes.domain.NoteChunk
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.TextFolder
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Hybrid search (`ROADMAP.md` 2.6), over the real content search and fakes of the index and the model. */
class SearchPassagesUseCaseTest {
    private val testDispatcher = StandardTestDispatcher()
    private val store = FakeNoteContentStore()
    private val index = FakeVectorIndex()
    private val embedder = FakeTextEmbedder()
    private val folder = TextFolder()
    private val cache = NoteTextCache(contentStore = store, textFolder = folder)
    private val search = SearchPassagesUseCase(
        textEmbedder = embedder,
        vectorIndex = index,
        noteTextCache = cache,
        textFolder = folder,
        markdownLineStripper = MarkdownLineStripper(),
        searchNoteContent = SearchNoteContentUseCase(
            noteTextCache = cache,
            textFolder = folder,
            naturalNameComparator = NaturalNameComparator(),
            markdownLineStripper = MarkdownLineStripper(),
            dispatchers = FakeAppDispatchers(dispatcher = testDispatcher)
        )
    )

    @Test
    fun `a note counts once, with its best line, the heading above it and the searched words marked`() = runTest(
        testDispatcher
    ) {
        val body = "## Dough\nKnead well.\nMix the **corn** flour.\n\n## Serving\nServe warm."
        val notes = listOf(note(path = AREPAS, body = body))
        indexed(
            path = AREPAS,
            body = body,
            heading = "Recipes",
            passage = "## Dough\nKnead well.\nMix the **corn** flour."
        )
        indexed(path = AREPAS, body = body, heading = "Serving", passage = "## Serving\nServe warm.", index = 1)

        val result = found(notes = notes, query = "corn").single()

        assertEquals(
            expected = listOf("Dough", "Mix the corn flour.", "corn"),
            actual = listOf(
                result.heading,
                result.snippet,
                result.snippet.substring(range = result.ranges.single())
            )
        )
        assertEquals(expected = "Mix the **corn** flour.", actual = body.substring(range = result.range))
    }

    @Test
    fun `a heading that repeats the note's name says nothing, and a long line is cut`() = runTest(testDispatcher) {
        val line = "Corn " + "flour ".repeat(n = 40)
        val notes = listOf(note(path = AREPAS, body = "# Arepas\n$line"))
        indexed(path = AREPAS, body = "# Arepas\n$line", passage = "# Arepas\n$line", heading = "Arepas")

        val result = found(notes = notes, query = "corn").single()

        assertEquals(expected = null to true, actual = result.heading to (result.snippet.length < line.length))
    }

    @Test
    fun `a chunk with no searched word stays only if it is close in meaning`() = runTest(testDispatcher) {
        val notes = listOf(note(path = AREPAS, body = "Corn flour."), note(path = SONG, body = "A slow guitar."))
        indexed(path = AREPAS, body = "Corn flour.", passage = "Corn flour.")
        indexed(path = SONG, body = "A slow guitar.", passage = "A slow guitar.")

        val result = found(notes = notes, query = "flour")

        assertEquals(expected = listOf(AREPAS), actual = result.map { it.note.file.path })
    }

    @Test
    fun `a tag keeps the notes that have it, and alone it needs no index`() = runTest(testDispatcher) {
        val notes = listOf(note(path = AREPAS, body = "Corn flour #recipe"), note(path = SONG, body = "Corn fields"))
        indexed(path = AREPAS, body = "Corn flour #recipe", passage = "Corn flour #recipe")
        indexed(path = SONG, body = "Corn fields", passage = "Corn fields")

        val tagged = found(notes = notes, query = "corn #recipe")
        embedder.embedded.clear()
        val tagAlone = found(notes = notes, query = "#recipe")

        assertEquals(
            expected = listOf(listOf(AREPAS), emptyList(), emptyList<Any>()),
            actual = listOf(tagged.map { it.note.file.path }, tagAlone, embedder.embedded)
        )
    }

    @Test
    fun `a chunk beyond the note's text, which changed after indexing, is left out`() = runTest(testDispatcher) {
        indexed(path = AREPAS, body = "Corn flour and more words.", passage = "Corn flour and more words.")
        store.texts[AREPAS] = "Corn"

        val result = found(notes = listOf(entry(path = AREPAS.value)), query = "corn")

        assertEquals(expected = emptyList(), actual = result)
    }

    @Test
    fun `a common word alone keeps nothing that is not close in meaning`() = runTest(testDispatcher) {
        val notes = listOf(note(path = SONG, body = "The slow guitar."))
        indexed(path = SONG, body = "The slow guitar.", passage = "The slow guitar.")

        assertEquals(expected = emptyList(), actual = found(notes = notes, query = "the football park"))
    }

    @Test
    fun `a chunk with no text left is skipped, and the others still answer`() = runTest(testDispatcher) {
        val body = "\n\n\nCorn flour."
        val notes = listOf(note(path = AREPAS, body = body), note(path = SONG, body = "Corn fields."))
        val blank = NoteChunk(AREPAS, 0, emptyList(), 0..1, "corn", tokens = 1)
        val vector = (embedder.embed(texts = listOf("corn")) as Outcome.Success).value.single()
        index.put(VAULT, AREPAS, listOf(blank), listOf(vector))
        indexed(path = SONG, body = "Corn fields.", passage = "Corn fields.")

        assertEquals(expected = listOf(SONG), actual = found(notes, "corn").map { it.note.file.path })
    }

    @Test
    fun `the snippet starts at the sentence with the word, accents or not`() = runTest(testDispatcher) {
        val body = "Empieza aquí. Una canción lenta. Y sigue."
        val notes = listOf(note(path = AREPAS, body = body))
        indexed(path = AREPAS, body = body, passage = body)

        assertEquals(
            expected = "Una canción lenta. Y sigue.",
            actual = found(notes = notes, query = "canción").single().snippet
        )
    }

    @Test
    fun `a model or an index that cannot answer says which`() = runTest(testDispatcher) {
        val notes = listOf(note(path = AREPAS, body = "Corn"))
        embedder.failure = EmbeddingError.EngineFailed

        val outcome = search(vaultPath = VAULT, notes = notes, query = "corn")

        assertEquals(expected = Outcome.Failure(error = IndexRunError.Model), actual = outcome)
    }

    private suspend fun found(
        notes: List<NoteEntry>,
        query: String
    ): List<PassageResult> = (search(VAULT, notes, query) as Outcome.Success).value

    private fun note(
        path: RelativePath,
        body: String
    ): NoteEntry = entry(path = path.value).also { store.texts[path] = body }

    private suspend fun indexed(
        path: RelativePath,
        body: String,
        passage: String,
        heading: String? = null,
        index: Int = 0
    ) {
        store.texts[path] = body
        val start = body.indexOf(string = passage)
        val chunk =
            NoteChunk(path, index, listOfNotNull(heading), start until start + passage.length, passage, tokens = 8)
        val vector = (embedder.embed(texts = listOf(passage)) as Outcome.Success).value
        val held = this.index.notes[VAULT]?.get(path).orEmpty()
        val chunks = held + (chunk to vector.single())
        this.index.put(VAULT, path, chunks.map { it.first }, chunks.map { it.second })
    }

    private companion object {
        val AREPAS = RelativePath(value = "Recipes/Arepas.md")
        val SONG = RelativePath(value = "Song.md")
    }
}
