package app.notekea.chat.presentation

import app.notekea.chat.domain.FakeActionRunner
import app.notekea.chat.domain.FakeChatHistoryStore
import app.notekea.chat.domain.TurnOutcome
import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.presentation.model.ActionStatus
import app.notekea.chat.presentation.model.ChatEffect
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
import app.notekea.core.presentation.ViewModelTestBase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The one loop works every request of the whole vault (`docs/agent-loop.md`, step 5), and a question back comes with
 * its answers as buttons, the one chosen sent as the next message.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatLoopTest : ViewModelTestBase() {
    private val engine = FakeLlmEngine()
    private val store = FakeNoteContentStore()

    private var route = ROUTE
    private var call = ASK

    private val notes =
        listOf(entry(path = "Home/Budget 2026.md"), entry(path = "Work/Budget 2026.md"))

    @BeforeTest
    fun setUp() {
        engine.readiness = Outcome.Success(
            value = LlmModelInfo(isSmall = false)
        )

        engine.reply = { messages ->
            val asked = messages.first().text

            when {
                asked.startsWith(prefix = "You say how a request") -> route
                asked.startsWith(prefix = "You work on a task in the person's notes") -> call
                else -> "Hello."
            }
        }
    }

    private fun TestScope.asked(request: String = "open the budget"): ChatViewModel {
        val viewModel = chatViewModel(
            engine = engine,
            store = store,
            index = FakeVectorIndex(),
            embedder = FakeTextEmbedder(),
            runner = FakeActionRunner(),
            history = FakeChatHistoryStore(),
            clipboard = FakeTextClipboard()
        )

        viewModel.onIntent(intent = ChatIntent.OnNotesChange(notes = notes))
        viewModel.onIntent(intent = ChatIntent.OnIndexChange(isReady = true))
        advanceUntilIdle()
        viewModel.onIntent(intent = ChatIntent.OnQuestionChange(text = request))
        viewModel.onIntent(intent = ChatIntent.OnSend)
        advanceUntilIdle()

        return viewModel
    }

    @Test
    fun `the loop asks back with the answers as buttons, and the one chosen is the next message`() =
        runTest(testDispatcher) {
            val viewModel = asked()

            val asked = viewModel
                .state
                .value
                .entries
                .last()

            viewModel.onIntent(intent = ChatIntent.OnFollowUp(text = asked.options.first()))
            advanceUntilIdle()

            assertEquals(
                expected = listOf("Which budget?", listOf("Home/Budget 2026", "Work/Budget 2026"), "Home/Budget 2026"),
                actual = listOf(
                    asked.answer,
                    asked.options,
                    viewModel
                        .state
                        .value
                        .entries
                        .last()
                        .question
                )
            )
        }

    @Test
    fun `a greeting the loop hands to the chat's own talk`() = runTest(testDispatcher) {
        route = """{"work":"talk","skill":"none"}"""

        val viewModel = asked(request = "hello there")

        val entry = viewModel
            .state
            .value
            .entries
            .last()

        assertEquals(
            expected = listOf<Any?>("Hello.", emptyList<Any>(), TurnOutcome.ANSWERED),
            actual = listOf(entry.answer, entry.moves, entry.outcome)
        )
    }

    @Test
    fun `a note asked to be opened is shown at once, with no card to confirm`() =
        runTest(testDispatcher) {
            route = """{"work":"app","skill":"none"}"""
            call = """{"tool":"open_note","note":"Home/Budget 2026"}"""

            val viewModel = asked()

            val shown = viewModel.effects.first() as ChatEffect.Show

            assertEquals(
                expected = listOf(
                    ActionKind.OPEN_NOTE,
                    listOf("Home/Budget 2026.md"),
                    ActionStatus.FINISHED
                ),
                actual = listOf(
                    shown.step.kind,
                    shown
                        .step
                        .notes
                        .map { it.value },
                    viewModel
                        .state
                        .value
                        .entries
                        .last()
                        .action
                        ?.status
                )
            )
        }

    private companion object {
        const val ROUTE = """{"work":"ask","skill":"none"}"""
        const val ASK =
            """{"tool":"ask_user","question":"Which budget?","options":["Home/Budget 2026","Work/Budget 2026"]}"""
    }
}
