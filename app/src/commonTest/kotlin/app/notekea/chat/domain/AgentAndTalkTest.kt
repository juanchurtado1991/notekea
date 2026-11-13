package app.notekea.chat.domain

import app.notekea.chat.data.GhostActionPlanReader
import app.notekea.chat.domain.agent.AgentAnswerUseCase
import app.notekea.chat.domain.answer.AnswerFromNotesUseCase
import app.notekea.chat.domain.answer.AnswerStep
import app.notekea.chat.domain.answer.ConverseUseCase
import app.notekea.chat.domain.plan.ActionContext
import app.notekea.chat.domain.plan.ActionField
import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.PlanRequestUseCase
import app.notekea.core.domain.Outcome
import app.notekea.core.embedding.domain.FakeTextEmbedder
import app.notekea.core.llm.domain.FakeLlmEngine
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

/** The agent's second try, talk instead of an answer, and a follow-up searched as it stands alone. */
class AgentAndTalkTest {
    private val store = FakeNoteContentStore()
    private val index = FakeVectorIndex()
    private val embedder = FakeTextEmbedder()
    private val engine = FakeLlmEngine()
    private val notes = mutableListOf<NoteEntry>()
    private val cache = NoteTextCache(
        contentStore = store,
        textFolder = TextFolder()
    )
    private val answer = AnswerFromNotesUseCase(embedder, index, cache, engine)
    private val agent = AgentAnswerUseCase(answer, cache, engine, GhostActionPlanReader())
    private val moves = ArrayDeque(elements = listOf(SEARCH_CORN, ANSWER_MOVE))

    @Test
    fun `when the first search finds nothing, the agent searches other words and answers with a citation`() =
        runTest {
            indexed(
                path = AREPAS,
                body = AREPAS_BODY
            )

            engine.scoreOf = { query, text ->
                if ("corn" in query && "corn" in text) 4f else -10f
            }

            engine.reply = { messages ->
                val isMove = messages
                    .first()
                    .text
                    .startsWith(prefix = "A first search")

                if (isMove) moves.removeFirst() else "Use corn flour [1]."
            }

            val first = answer(
                vaultPath = VAULT,
                notes = notes,
                question = "What maize goes in?"
            ).toList().last()

            val second = agent(
                vaultPath = VAULT,
                notes = notes,
                question = "What maize goes in?"
            ).toList().last()

            assertEquals(
                expected = AnswerStep.NotFound to listOf(AREPAS),
                actual = first to (second as AnswerStep.Done)
                    .answer
                    .sources
                    .map { it.note }
            )
        }

    @Test
    fun `talk about a note reads it and shows it as the source, and a follow-up is searched alone`() =
        runTest {
            indexed(
                path = AREPAS,
                body = AREPAS_BODY
            )

            engine.reply = { "I like it: it is short and clear." }

            val talk = ConverseUseCase(engine, cache)(
                message = "what do you think of it?",
                history = listOf(
                    ChatExchange(
                        question = "How do I make arepas?",
                        reply = "Mix corn flour."
                    )
                ),
                vaultPath = VAULT,
                note = notes.single()
            ).toList().last() as AnswerStep.Done

            engine.reply = { "{\"steps\":[{\"action\":\"answer\",\"request\":\"How long does the arepa dough rest?\"}]}" }

            val plan = PlanRequestUseCase(engine, GhostActionPlanReader())(
                request = "and how long does it rest?",
                context = ActionContext(today = "2026-11-10")
            ) as Outcome.Success

            assertEquals(
                expected = listOf(AREPAS) to "How long does the arepa dough rest?",
                actual = talk
                    .answer
                    .sources
                    .map { it.note } to plan.value.single()[ActionField.REQUEST]
            )
        }

    @Test
    fun `a greeting with an action still proposes the action`() = runTest {
        engine.reply = {
            "{\"steps\":[{\"action\":\"chat\"},{\"action\":\"create_note\",\"title\":\"Compras\"}]}"
        }

        val plan = PlanRequestUseCase(engine, GhostActionPlanReader())(
            request = "hola, crea una nota Compras",
            context = ActionContext(today = "2026-11-10")
        ) as Outcome.Success

        assertEquals(
            expected = listOf(ActionKind.CREATE_NOTE),
            actual = plan.value.map { it.kind }
        )
    }

    private suspend fun indexed(
        path: RelativePath,
        body: String
    ) {
        store.texts[path] = body
        notes += entry(path = path.value)
        val chunk = NoteChunk(path, 0, emptyList(), body.indices, body, tokens = 8)
        val vector = (embedder.embed(texts = listOf(body)) as Outcome.Success).value.single()
        index.put(VAULT, path, listOf(chunk), listOf(vector))
    }

    private companion object {
        val AREPAS = RelativePath(value = "Recipes/Arepas.md")
        const val AREPAS_BODY = "Mix corn flour with warm water and salt."
        const val SEARCH_CORN = "{\"move\":\"search\",\"query\":\"corn flour\"}"
        const val ANSWER_MOVE = "{\"move\":\"answer\"}"
    }
}
