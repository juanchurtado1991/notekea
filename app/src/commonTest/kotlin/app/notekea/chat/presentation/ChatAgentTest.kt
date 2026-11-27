package app.notekea.chat.presentation

import app.notekea.chat.domain.FakeActionRunner
import app.notekea.chat.domain.FakeChatHistoryStore
import app.notekea.chat.domain.TurnOutcome
import app.notekea.chat.presentation.model.ActionStatus
import app.notekea.chat.presentation.model.AgentMoveLine
import app.notekea.chat.presentation.model.ChatEntry
import app.notekea.chat.presentation.model.ChatIntent
import app.notekea.chat.presentation.viewmodel.ChatViewModel
import app.notekea.core.domain.Outcome
import app.notekea.core.embedding.domain.FakeTextEmbedder
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.llm.domain.LlmModelInfo
import app.notekea.core.logging.domain.FakeTextClipboard
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.FakeVectorIndex
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.ViewModelTestBase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** The agent in the chat (4.7): the loop works every request of the whole vault, its moves listed, its end a card. */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatAgentTest : ViewModelTestBase() {
    private val engine = FakeLlmEngine()
    private val runner = FakeActionRunner()
    private val store = FakeNoteContentStore()
    private val calls = ArrayDeque<String>()
    private var route = ORGANIZE

    @BeforeTest
    fun setUp() {
        store.texts[RelativePath(value = "Books/Dune.md")] = "Dune is a novel."

        engine.reply = { messages ->
            val asked = messages.first().text

            when {
                asked.startsWith(prefix = "You say how a request") -> route

                asked.startsWith(prefix = CALLS) -> calls.removeFirst()

                asked.startsWith(prefix = OVERVIEW_PROMPT) -> "Books, mostly science fiction."

                // The answer written from what was found: here the notes hold nothing of what is asked.
                asked.startsWith(prefix = "Write the answer to the task") -> "NOT_FOUND"

                else -> "Hello."
            }
        }
    }

    private fun TestScope.started(): ChatViewModel {
        engine.readiness = Outcome.Success(
            value = LlmModelInfo(isSmall = false)
        )

        val viewModel = chatViewModel(
            engine = engine,
            store = store,
            index = FakeVectorIndex(),
            embedder = FakeTextEmbedder(),
            runner = runner,
            history = FakeChatHistoryStore(),
            clipboard = FakeTextClipboard()
        )

        viewModel.onIntent(intent = ChatIntent.OnNotesChange(notes = listOf(entry(path = "Books/Dune.md"))))
        viewModel.onIntent(intent = ChatIntent.OnIndexChange(isReady = true))
        advanceUntilIdle()

        return viewModel
    }

    private fun TestScope.asked(
        viewModel: ChatViewModel,
        request: String = "tag Dune with scifi"
    ): ChatEntry {
        viewModel.onIntent(intent = ChatIntent.OnQuestionChange(text = request))
        viewModel.onIntent(intent = ChatIntent.OnSend)
        advanceUntilIdle()

        return viewModel
            .state
            .value
            .entries
            .last()
    }

    @Test
    fun `the agent lists its moves and ends on a card, and nothing is written before it is confirmed`() =
        runTest(testDispatcher) {
            calls += """{"tool":"read","note":"Dune"}"""
            calls += """{"tool":"add_tag","notes":["Books/Dune"],"tag":"scifi"}"""
            calls += """{"tool":"propose","summary":"Tag Dune."}"""

            val entry = asked(viewModel = started())

            assertEquals(
                expected = listOf<Any?>(
                    // The proposal is the turn's end, not a move.
                    listOf("read", "add_tag"),
                    ActionStatus.PROPOSED,
                    "Tag Dune.",
                    0
                ),
                actual = listOf(
                    entry.moves.map { it.move },
                    entry.action?.status,
                    entry.answer,
                    runner.ran.size
                )
            )
        }

    // The developer's chat of 2026-11-11: "what are my notes about?" went to the agent, which searched for the word
    // "notes"; the loop hands the vault's overview to the chat's own reply.
    @Test
    fun `the vault's overview is the chat's own reply, with no move of the agent`() =
        runTest(testDispatcher) {
            route = """{"work":"overview","skill":"none"}"""

            val entry = asked(
                viewModel = started(),
                request = "me podes contar de que tratan las notas?"
            )

            assertEquals(
                expected = listOf<Any?>(
                    "Books, mostly science fiction.",
                    emptyList<AgentMoveLine>()
                ),
                actual = listOf(entry.answer, entry.moves)
            )
        }

    @Test
    fun `an agent that finds nothing ends on the not found reply, not its own words`() =
        runTest(testDispatcher) {
            route = """{"work":"ask","skill":"none"}"""
            calls += """{"tool":"search","query":"dentist"}"""
            calls += """{"tool":"read","note":"Dune"}"""
            calls += """{"tool":"answer","text":"No dentist appointment was found in your notes."}"""

            val entry = asked(
                viewModel = started(),
                request = "When is my dentist appointment?"
            )

            assertEquals(
                expected = listOf<Any?>(TurnOutcome.NOT_FOUND, ""),
                actual = listOf(entry.outcome, entry.answer)
            )
        }

    private companion object {
        const val CALLS = "You work on a task in the person's notes"
        const val ORGANIZE = """{"work":"organize","skill":"none"}"""
        const val OVERVIEW_PROMPT = "You are the assistant of Notekea, a notes app. Say"
    }
}
