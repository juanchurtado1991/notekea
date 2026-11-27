package app.notekea.chat.domain

import app.notekea.chat.domain.answer.AnswerSource
import app.notekea.chat.domain.answer.PassageSearch
import app.notekea.core.domain.Outcome
import app.notekea.core.embedding.domain.EmbeddingError
import app.notekea.core.embedding.domain.FakeTextEmbedder
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.llm.domain.LlmError
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.FakeVectorIndex
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.index.NoteChunk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** The passages a search finds, reranked, over fakes of the index, the embedding model and the reranker. */
class PassageSearchTest {
    private val store = FakeNoteContentStore()
    private val index = FakeVectorIndex()
    private val embedder = FakeTextEmbedder()
    private val engine = FakeLlmEngine()
    private val notes = mutableListOf<NoteEntry>()

    private val search = PassageSearch(
        textEmbedder = embedder,
        vectorIndex = index,
        noteTextCache = NoteTextCache(contentStore = store),
        llmEngine = engine
    )

    @Test
    fun `the best passages are found, reranked and numbered from 1`() = runTest {
        indexed(
            path = AREPAS,
            body = AREPAS_BODY
        )

        indexed(
            path = BEANS,
            body = BEANS_BODY
        )

        engine.scoreOf = { _, text ->
            if ("corn" in text) 4f else -2f
        }

        val found = excerpts(query = "What goes in the arepa dough?")

        assertEquals(
            expected = listOf(1 to AREPAS, 2 to BEANS),
            actual = found.map {
                it.number to it.note
            }
        )
    }

    @Test
    fun `nothing close enough is no passage, and the model never writes`() = runTest {
        indexed(
            path = AREPAS,
            body = AREPAS_BODY
        )

        engine.scoreOf = { _, _ ->
            -9f
        }

        assertEquals(
            expected = listOf<Any>(emptyList<AnswerSource>(), true),
            actual = listOf(excerpts(query = "When is the dentist?"), engine.generated.isEmpty())
        )
    }

    @Test
    fun `an empty vault finds nothing, and a model or an index that fails says which`() = runTest {
        val empty = passages(query = "Anything?")

        indexed(
            path = AREPAS,
            body = AREPAS_BODY
        )

        engine.failure = LlmError.NoModel
        val noModel = passages(query = "Flour?")
        embedder.failure = EmbeddingError.EngineFailed

        assertEquals(
            expected = listOf(
                Outcome.Success(value = emptyList()),
                Outcome.Failure(error = ChatError.Model(error = LlmError.NoModel)),
                Outcome.Failure(error = ChatError.Search)
            ),
            actual = listOf(empty, noModel, passages(query = "Flour?"))
        )
    }

    private suspend fun passages(query: String): Outcome<List<AnswerSource>, ChatError> =
        search.excerpts(
            vaultPath = VAULT,
            notes = notes,
            query = query
        )

    private suspend fun excerpts(query: String): List<AnswerSource> =
        (passages(query = query) as Outcome.Success).value

    // The developer's doubt of 2026-11-14: a citation of a clipped page could be of the summary a model wrote about
    // it, which may be wrong, and read as the page's own words.
    @Test
    fun `a clipped page is cited from its own text, never from the notes a model wrote about it`() =
        runTest {
            val clip = RelativePath(value = "Web sources/Outage.md")
            val summary = "The outage lasted two hours.\n\n"
            val original = "## Original text\n\nThe outage lasted nine hours, the company said."
            val body = summary + original

            store.texts[clip] = body
            notes += entry(path = clip.value)

            val chunks = listOf(
                NoteChunk(clip, 0, emptyList(), summary.indices, summary, tokens = 8),
                NoteChunk(
                    clip,
                    1,
                    listOf("Original text"),
                    summary.length until body.length,
                    original,
                    tokens = 8
                )
            )

            val vectors = (embedder.embed(texts = chunks.map { it.text }) as Outcome.Success).value
            index.put(VAULT, clip, chunks, vectors)
            engine.scoreOf = { _, _ -> 4f }

            val read = search.excerpts(
                vaultPath = VAULT,
                notes = notes,
                query = "How long did the outage last?"
            )

            assertEquals(
                expected = listOf(original),
                actual = (read as Outcome.Success).value.map { it.text }
            )
        }

    private suspend fun indexed(
        path: RelativePath,
        body: String
    ) {
        store.texts[path] = body
        notes += entry(path = path.value)

        val chunk = NoteChunk(
            path,
            0,
            emptyList(),
            body.indices,
            body,
            tokens = 8
        )

        val vector = (embedder.embed(texts = listOf(body)) as Outcome.Success).value.single()
        index.put(VAULT, path, listOf(chunk), listOf(vector))
    }

    private companion object {
        val AREPAS = RelativePath(value = "Recipes/Arepas.md")
        val BEANS = RelativePath(value = "Recipes/Black beans.md")
        const val AREPAS_BODY = "Mix corn flour with warm water and salt."
        const val BEANS_BODY = "Soak the beans overnight, then cook them with flour of plantain."
    }
}
