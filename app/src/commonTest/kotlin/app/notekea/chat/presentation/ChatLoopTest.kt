package app.notekea.chat.presentation

import app.notekea.chat.data.GhostActionPlanReader
import app.notekea.chat.domain.FakeActionRunner
import app.notekea.chat.domain.FakeChatHistoryStore
import app.notekea.chat.domain.TurnOutcome
import app.notekea.chat.domain.agent.ProposalCheck
import app.notekea.chat.domain.answer.AnswerFromNotesUseCase
import app.notekea.chat.domain.loop.ChatLoop
import app.notekea.chat.domain.loop.LoopTools
import app.notekea.chat.domain.loop.RunTurnUseCase
import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.CheckPlanUseCase
import app.notekea.chat.domain.plan.ComposeNotesUseCase
import app.notekea.chat.domain.plan.PageProblem
import app.notekea.chat.domain.skill.VaultGuide
import app.notekea.chat.presentation.model.ActionStatus
import app.notekea.chat.presentation.model.ChatEffect
import app.notekea.chat.presentation.model.ChatIntent
import app.notekea.chat.presentation.model.ChatMode
import app.notekea.chat.presentation.viewmodel.ChatViewModel
import app.notekea.core.domain.Outcome
import app.notekea.core.embedding.domain.FakeTextEmbedder
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.llm.domain.LlmModelInfo
import app.notekea.core.logging.domain.FakeTextClipboard
import app.notekea.core.notes.data.JetBrainsMarkdownLinkFinder
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.FakeVectorIndex
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.TextFolder
import app.notekea.core.preferences.domain.AppPreferences
import app.notekea.core.preferences.domain.FakeAppPreferencesStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
import kotlin.time.Clock

/**
 * Agent mode with the one loop turned on in Settings (`docs/agent-loop.md`, step 5): the loop works the request,
 * and a question back comes with its answers as buttons, the one chosen sent as the next message.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatLoopTest {
    private val dispatcher = StandardTestDispatcher()
    private val engine = FakeLlmEngine()
    private val store = FakeNoteContentStore()
    private val cache = NoteTextCache(
        contentStore = store,
        textFolder = TextFolder()
    )

    private var route = ROUTE
    private var call = ASK

    private val notes =
        listOf(entry(path = "Home/Budget 2026.md"), entry(path = "Work/Budget 2026.md"))

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = dispatcher)

        engine.readiness = Outcome.Success(
            value = LlmModelInfo(
                isSmall = false,
                hasAgent = true
            )
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

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun loopOf(isOn: Boolean): ChatLoop {
        val tools = LoopTools(
            llmEngine = engine,
            answerFromNotes = AnswerFromNotesUseCase(
                FakeTextEmbedder(),
                FakeVectorIndex(),
                cache,
                engine
            ),
            noteTextCache = cache,
            actionRunner = FakeActionRunner(),
            pageReader = { Outcome.Failure(error = PageProblem.OFFLINE) },
            linkFinder = JetBrainsMarkdownLinkFinder()
        )

        val runTurn = RunTurnUseCase(
            llmEngine = engine,
            toolsets = tools.toolsets(),
            vaultGuide = VaultGuide(noteTextCache = cache),
            proposal = ProposalCheck(
                checkPlan = CheckPlanUseCase(),
                composeNotes = ComposeNotesUseCase(engine, cache, GhostActionPlanReader())
            ),
            clock = Clock.System
        )

        return ChatLoop(
            runTurn = runTurn,
            preferencesStore = FakeAppPreferencesStore(initial = AppPreferences(chatLoop = isOn))
        )
    }

    private fun TestScope.asked(
        isOn: Boolean,
        request: String = "open the budget",
        mode: ChatMode = ChatMode.AGENT
    ): ChatViewModel {
        val viewModel = chatViewModel(
            engine = engine,
            store = store,
            index = FakeVectorIndex(),
            embedder = FakeTextEmbedder(),
            runner = FakeActionRunner(),
            history = FakeChatHistoryStore(),
            clipboard = FakeTextClipboard(),
            loop = loopOf(isOn = isOn)
        )

        viewModel.onIntent(intent = ChatIntent.OnNotesChange(notes = notes))
        viewModel.onIntent(intent = ChatIntent.OnIndexChange(isReady = true))
        advanceUntilIdle()
        viewModel.onIntent(intent = ChatIntent.OnModeChoose(mode = mode))
        viewModel.onIntent(intent = ChatIntent.OnQuestionChange(text = request))
        viewModel.onIntent(intent = ChatIntent.OnSend)
        advanceUntilIdle()

        return viewModel
    }

    @Test
    fun `turned on, the loop asks back with the answers as buttons, and the one chosen is the next message`() =
        runTest(dispatcher) {
            val viewModel = asked(isOn = true)

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
    fun `turned off, agent mode is today's agent, which never asks with buttons`() =
        runTest(dispatcher) {
            val viewModel = asked(isOn = false)

            assertEquals(
                expected = emptyList(),
                actual = viewModel
                    .state
                    .value
                    .entries
                    .last()
                    .options
            )
        }

    @Test
    fun `turned on, it takes a request in Auto too, and a greeting it hands to the chat's own talk`() =
        runTest(dispatcher) {
            route = """{"work":"talk","skill":"none"}"""

            val viewModel = asked(
                isOn = true,
                request = "hello there",
                mode = ChatMode.AUTO
            )

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
    fun `turned on, a note asked to be opened is shown at once, with no card to confirm`() =
        runTest(dispatcher) {
            route = """{"work":"app","skill":"none"}"""
            call = """{"tool":"open_note","note":"Home/Budget 2026"}"""

            val viewModel = asked(
                isOn = true,
                mode = ChatMode.AUTO
            )

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
