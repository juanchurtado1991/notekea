package app.notekea.chat.presentation

import app.notekea.chat.domain.FakeActionRunner
import app.notekea.chat.domain.FakeChatHistoryStore
import app.notekea.chat.domain.TurnOutcome
import app.notekea.chat.presentation.model.ActionStatus
import app.notekea.chat.presentation.model.AgentMoveLine
import app.notekea.chat.presentation.model.ChatEntry
import app.notekea.chat.presentation.model.ChatIntent
import app.notekea.chat.presentation.model.ChatMode
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** Agent mode in the chat (4.7): offered only for the sizes that have it, its moves listed, its end a card. */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatAgentTest {
    private val dispatcher = StandardTestDispatcher()
    private val engine = FakeLlmEngine()
    private val runner = FakeActionRunner()
    private val store = FakeNoteContentStore()
    private val moves = ArrayDeque<String>()
    private var plan = "{\"steps\":[{\"action\":\"add_tag\",\"note\":\"Dune\",\"tag\":\"scifi\"}]}"

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = dispatcher)
        store.texts[RelativePath(value = "Books/Dune.md")] = "Dune is a novel."

        engine.reply = { messages ->
            when {
                messages.first().text.startsWith("You work on a task") -> moves.removeFirst()
                messages.first().text.startsWith("You turn a request") -> plan
                messages.first().text.startsWith(OVERVIEW_PROMPT) -> "Books, mostly science fiction."
                else -> "Hello."
            }
        }
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.started(hasAgent: Boolean): ChatViewModel {
        engine.readiness = Outcome.Success(
            value = LlmModelInfo(
                isSmall = false,
                hasAgent = hasAgent
            )
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
        mode: ChatMode = ChatMode.AGENT,
        request: String = "tag Dune with scifi"
    ): ChatEntry {
        viewModel.onIntent(intent = ChatIntent.OnModeChoose(mode = mode))
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
        runTest(dispatcher) {
            moves += "{\"move\":\"read\",\"note\":\"Dune\"}"

            moves += "{\"move\":\"propose\",\"summary\":\"Tag Dune.\",\"steps\":" +
                "[{\"action\":\"add_tag\",\"note\":\"Dune\",\"tag\":\"scifi\"}]}"

            val entry = asked(viewModel = started(hasAgent = true))

            assertEquals(
                expected = listOf<Any?>(
                    listOf(
                        AgentMoveLine(
                            move = "read",
                            target = "Books/Dune"
                        )
                    ),
                    ActionStatus.PROPOSED,
                    "Tag Dune.",
                    0
                ),
                actual = listOf(entry.moves, entry.action?.status, entry.answer, runner.ran.size)
            )
        }

    @Test
    fun `a size without agent mode answers as usual even with the toggle on`() =
        runTest(dispatcher) {
            plan = TALK
            val viewModel = started(hasAgent = false)
            val entry = asked(viewModel = viewModel)

            assertEquals(
                expected = listOf<Any?>(false, emptyList<AgentMoveLine>(), TurnOutcome.ANSWERED, "Hello."),
                actual = listOf(
                    viewModel.state.value.isAgentOffered,
                    entry.moves,
                    entry.outcome,
                    entry.answer
                )
            )
        }

    // The developer's chat of 2026-11-11: with Agent mode on, "what are my notes about?" went to the agent, which
    // searched for the word "notes"; the plan describes the vault.
    @Test
    fun `in agent mode the vault's overview, help and talk are the plan's, not the agent's`() =
        runTest(dispatcher) {
            plan = "{\"steps\":[{\"action\":\"overview\"}]}"

            val entry = asked(
                viewModel = started(hasAgent = true),
                request = "me podes contar de que tratan las notas?"
            )

            assertEquals(
                expected = listOf<Any?>(
                    "Books, mostly science fiction.",
                    emptyList<AgentMoveLine>(),
                    ChatMode.FAST
                ),
                actual = listOf(entry.answer, entry.moves, entry.answeredWith)
            )
        }

    @Test
    fun `auto hands the agent an action whose note the plan could not name, and only where it is offered`() =
        runTest(dispatcher) {
            plan = "{\"steps\":[{\"action\":\"add_tag\",\"note\":\"the desert novel\",\"tag\":\"scifi\"}]}"
            moves += "{\"move\":\"search\",\"query\":\"desert novel\"}"
            moves += "{\"move\":\"finish\",\"summary\":\"Dune.\"}"

            val withAgent = asked(
                viewModel = started(hasAgent = true),
                mode = ChatMode.AUTO
            )

            val without = asked(
                viewModel = started(hasAgent = false),
                mode = ChatMode.AUTO
            )

            assertEquals(
                expected = listOf<Any?>(ChatMode.AGENT, 1, ActionStatus.FAILED, null),
                actual = listOf(
                    withAgent.answeredWith,
                    withAgent.moves.size,
                    without.action?.status,
                    without.answeredWith
                )
            )
        }

    private companion object {
        const val TALK = "{\"steps\":[{\"action\":\"chat\"}]}"
        const val OVERVIEW_PROMPT = "You are the assistant of Notekea, a notes app. Say"
    }
}
