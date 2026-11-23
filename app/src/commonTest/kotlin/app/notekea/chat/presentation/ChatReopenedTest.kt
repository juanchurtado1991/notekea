package app.notekea.chat.presentation

import app.notekea.chat.domain.ChatScope
import app.notekea.chat.domain.ChatTurn
import app.notekea.chat.domain.FakeActionRunner
import app.notekea.chat.domain.FakeChatHistoryStore
import app.notekea.chat.domain.TurnOutcome
import app.notekea.chat.domain.conversation.Conversation
import app.notekea.chat.domain.conversation.ProposalOutcome
import app.notekea.chat.domain.conversation.ProposedChange
import app.notekea.chat.domain.conversation.TurnItem
import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.presentation.model.AgentMoveLine
import app.notekea.chat.presentation.model.ChatIntent
import app.notekea.chat.presentation.viewmodel.ChatViewModel
import app.notekea.core.embedding.domain.FakeTextEmbedder
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.logging.domain.FakeTextClipboard
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.FakeVectorIndex
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.ViewModelTestBase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The chat kept as a conversation (`docs/agent-loop.md`, step 3): opened again it knows what it looked up and
 * proposed and how that ended, and a turn the app was closed on is there, stopped.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatReopenedTest : ViewModelTestBase() {
    private val engine = FakeLlmEngine()
    private val history = FakeChatHistoryStore()

    private val worked = ChatTurn(
        question = "make a table of the contract",
        scope = ChatScope.VAULT,
        outcome = TurnOutcome.ACTED,
        items = listOf(
            TurnItem.Call(
                name = "read",
                target = "Contract/contract"
            ),
            TurnItem.Proposal(
                changes = listOf(
                    ProposedChange(
                        kind = ActionKind.WRITE_FROM_NOTES,
                        notes = listOf(RelativePath(value = "Contract/contract.md")),
                        start = "| Pros | Cons |",
                        title = "Pros and cons"
                    )
                ),
                outcome = ProposalOutcome.CANCELLED,
                problem = null
            )
        ),
        scopeName = null
    )

    private fun TestScope.started(): ChatViewModel {
        val viewModel = chatViewModel(
            engine = engine,
            store = FakeNoteContentStore(),
            index = FakeVectorIndex(),
            embedder = FakeTextEmbedder(),
            runner = FakeActionRunner(),
            history = history,
            clipboard = FakeTextClipboard()
        )

        viewModel.onIntent(intent = ChatIntent.OnNotesChange(notes = emptyList()))
        viewModel.onIntent(intent = ChatIntent.OnIndexChange(isReady = true))
        advanceUntilIdle()

        return viewModel
    }

    private fun TestScope.ask(
        viewModel: ChatViewModel,
        question: String
    ) {
        viewModel.onIntent(intent = ChatIntent.OnQuestionChange(text = question))
        viewModel.onIntent(intent = ChatIntent.OnSend)
        advanceUntilIdle()
    }

    @Test
    fun `a chat opened again shows what it looked up and how its proposal ended, and keeps them`() =
        runTest(testDispatcher) {
            history.kept[VAULT] = Conversation(turns = listOf(worked))
            val viewModel = started()

            val shown = viewModel
                .state
                .value
                .entries
                .single()

            ask(
                viewModel = viewModel,
                question = "hello"
            )

            assertEquals(
                expected = listOf(
                    listOf(
                        AgentMoveLine(
                            move = "read",
                            target = "Contract/contract"
                        )
                    ),
                    ProposalOutcome.CANCELLED,
                    worked
                ),
                actual = listOf(
                    shown.moves,
                    shown.proposal?.outcome,
                    history
                        .turns
                        .getValue(VAULT)
                        .first()
                )
            )
        }

    @Test
    fun `asked after it is opened again, the model is told the summary and what the chat did`() =
        runTest(testDispatcher) {
            history.kept[VAULT] = Conversation(
                turns = listOf(worked.copy(question = "an older question"), worked),
                summary = "They asked about the lease.",
                compacted = 1
            )

            val viewModel = started()

            ask(
                viewModel = viewModel,
                question = "did you make that table up?"
            )

            val told = engine
                .generated
                .joinToString(separator = "\n") { messages ->
                    messages.joinToString(separator = "\n") { it.text }
                }

            assertEquals(
                expected = listOf(true, true, false),
                actual = listOf(
                    "They asked about the lease." in told,
                    "I looked up: read Contract/contract. The person cancelled it." in told,
                    "an older question" in told
                )
            )
        }

    @Test
    fun `a question is kept from the moment it is asked, as stopped until it is answered`() =
        runTest(testDispatcher) {
            val keptWhileAnswering = mutableListOf<TurnOutcome?>()

            engine.reply = {
                keptWhileAnswering += history
                    .turns[VAULT]
                    ?.lastOrNull()
                    ?.outcome

                "Hello."
            }

            val viewModel = started()

            ask(
                viewModel = viewModel,
                question = "hello"
            )

            assertEquals(
                expected = listOf(TurnOutcome.STOPPED, true),
                actual = listOf(
                    keptWhileAnswering.first(),
                    history
                        .turns
                        .getValue(VAULT)
                        .single()
                        .outcome != TurnOutcome.STOPPED
                )
            )
        }
}
