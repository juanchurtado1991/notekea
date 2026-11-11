package app.notekea.chat.presentation

import app.notekea.chat.domain.FakeActionRunner
import app.notekea.chat.domain.FakeChatHistoryStore
import app.notekea.chat.domain.TurnOutcome
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

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = dispatcher)
        store.texts[RelativePath(value = "Books/Dune.md")] = "Dune is a novel."

        engine.reply = { messages ->
            when {
                messages.first().text.startsWith("You work on a task") -> moves.removeFirst()
                messages.first().text.startsWith("You turn a request") -> "{\"steps\":[{\"action\":\"chat\"}]}"
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

    private fun TestScope.asked(viewModel: ChatViewModel): ChatEntry {
        viewModel.onIntent(intent = ChatIntent.OnAgentToggle)
        viewModel.onIntent(intent = ChatIntent.OnQuestionChange(text = "tag Dune with scifi"))
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
                            target = "Dune"
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
}
