package app.notekea.chat.domain

import app.notekea.chat.domain.answer.AnswerFromNotesUseCase
import app.notekea.chat.domain.answer.AnswerStep
import app.notekea.core.domain.Outcome
import app.notekea.core.embedding.domain.EmbeddingError
import app.notekea.core.embedding.domain.FakeTextEmbedder
import app.notekea.core.llm.domain.ChatRole
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.llm.domain.LlmError
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.FakeVectorIndex
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.TextFolder
import app.notekea.core.notes.domain.index.NoteChunk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** One call over the reranked passages, over fakes of the index, the embedding model and the chat model. */
class AnswerFromNotesUseCaseTest {
    private val store = FakeNoteContentStore()
    private val index = FakeVectorIndex()
    private val embedder = FakeTextEmbedder()
    private val engine = FakeLlmEngine()
    private val notes = mutableListOf<NoteEntry>()

    private val answer = AnswerFromNotesUseCase(
        textEmbedder = embedder,
        vectorIndex = index,
        noteTextCache = NoteTextCache(
            contentStore = store,
            textFolder = TextFolder()
        ),
        llmEngine = engine
    )

    @Test
    fun `the best passages are read, the answer streams in and ends with its sources renumbered`() =
        runTest {
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

            engine.reply = { "Use corn flour and warm water [1]." }

            val steps = steps(question = "What goes in the arepa dough?")

            assertEquals(
                expected = AnswerStep.Searching to AnswerStep.Reading(notes = 2),
                actual = steps[0] to steps[1]
            )

            assertTrue(actual = steps.filterIsInstance<AnswerStep.Writing>().size >= 2)

            val done = (steps.last() as AnswerStep.Done).answer

            assertEquals(
                expected = "Use corn flour and warm water [1]." to listOf(AREPAS),
                actual = done.text to done.sources.map { it.note }
            )

            val prompt = engine.generated.single()

            assertEquals(
                expected = listOf(ChatRole.SYSTEM, ChatRole.USER),
                actual = prompt.map { it.role }
            )

            assertTrue(actual = prompt[1].text.startsWith(prefix = "Excerpts:\n[1] $AREPAS_BODY"))
        }

    @Test
    fun `nothing close enough is answered without the model, and the model's NOT_FOUND is never shown`() =
        runTest {
            indexed(
                path = AREPAS,
                body = AREPAS_BODY
            )

            engine.scoreOf = { _, _ ->
                -9f
            }

            assertEquals(
                expected = AnswerStep.NotFound,
                actual = steps(question = "When is the dentist?").last()
            )

            assertTrue(actual = engine.generated.isEmpty())

            engine.scoreOf = { _, _ -> 1f }
            engine.reply = { "NOT_FOUND" }
            val steps = steps(question = "When is the dentist?")

            assertEquals(
                expected = emptyList(),
                actual = steps.filterIsInstance<AnswerStep.Writing>()
            )

            assertEquals(
                expected = AnswerStep.NotFound,
                actual = steps.last()
            )
        }

    @Test
    fun `an empty vault finds nothing, and a model or an index that fails says which`() = runTest {
        assertEquals(
            expected = AnswerStep.NotFound,
            actual = steps(question = "Anything?").last()
        )

        indexed(
            path = AREPAS,
            body = AREPAS_BODY
        )

        engine.failure = LlmError.NoModel

        assertEquals(
            expected = AnswerStep.Failed(error = ChatError.Model(error = LlmError.NoModel)),
            actual = steps(question = "Flour?").last()
        )

        embedder.failure = EmbeddingError.EngineFailed

        assertEquals(
            expected = AnswerStep.Failed(error = ChatError.Search),
            actual = steps(question = "Flour?").last()
        )
    }

    @Test
    fun `only the notes asked for are searched`() = runTest {
        indexed(
            path = AREPAS,
            body = AREPAS_BODY
        )

        indexed(
            path = BEANS,
            body = BEANS_BODY
        )

        engine.reply = { "Soak them [1]." }

        val done = steps(
            question = "flour beans",
            only = setOf(BEANS)
        ).last() as AnswerStep.Done

        assertEquals(
            expected = listOf(BEANS),
            actual = done
                .answer
                .read
                .map { it.note }
        )
    }

    @Test
    fun `Deep answer writes three drafts with their own seeds and shows only the one voted for`() =
        runTest {
            indexed(
                path = AREPAS,
                body = AREPAS_BODY
            )

            val drafts = ArrayDeque(
                elements = listOf(
                    "Use corn flour and warm water [1].",
                    "Use rice flour [1].",
                    "Use corn flour with warm water [1]."
                )
            )

            engine.reply = { drafts.removeFirst() }

            val steps = steps(
                question = "What goes in the arepa dough?",
                drafts = 3
            )

            assertEquals(
                expected = Triple(
                    listOf(AnswerStep.Drafting(drafts = 3)),
                    "Use corn flour and warm water [1].",
                    listOf(1, 2, 3)
                ),
                actual = Triple(
                    steps.filterIsInstance<AnswerStep.Drafting>() +
                        steps.filterIsInstance<AnswerStep.Writing>(),
                    (steps.last() as AnswerStep.Done).answer.text,
                    engine.options.map { it.seed }
                )
            )
        }

    private suspend fun steps(
        question: String,
        only: Set<RelativePath>? = null,
        drafts: Int = 1
    ): List<AnswerStep> = answer(
        vaultPath = VAULT,
        notes = notes,
        question = question,
        only = only,
        drafts = drafts
    ).toList()

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
