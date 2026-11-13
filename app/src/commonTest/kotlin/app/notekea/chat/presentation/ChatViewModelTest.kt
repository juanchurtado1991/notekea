package app.notekea.chat.presentation

import app.notekea.chat.data.GhostActionPlanReader
import app.notekea.chat.domain.ChatScope
import app.notekea.chat.domain.ChatTurn
import app.notekea.chat.domain.FakeActionRunner
import app.notekea.chat.domain.FakeChatHistoryStore
import app.notekea.chat.domain.TurnOutcome
import app.notekea.chat.domain.agent.AgentAnswerUseCase
import app.notekea.chat.domain.answer.AnswerFromNotesUseCase
import app.notekea.chat.domain.answer.ConverseUseCase
import app.notekea.chat.domain.plan.ActOnRequestUseCase
import app.notekea.chat.domain.plan.CheckPlanUseCase
import app.notekea.chat.domain.plan.ComposeNotesUseCase
import app.notekea.chat.domain.plan.PlanRequestUseCase
import app.notekea.chat.presentation.model.ActionStatus
import app.notekea.chat.presentation.model.ChatEffect
import app.notekea.chat.presentation.model.ChatIntent
import app.notekea.chat.presentation.model.ModelStatus
import app.notekea.chat.presentation.viewmodel.ChatViewModel
import app.notekea.core.domain.Outcome
import app.notekea.core.domain.time.FakeLocalCalendar
import app.notekea.core.embedding.domain.FakeTextEmbedder
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.llm.domain.LlmError
import app.notekea.core.llm.domain.LlmModelInfo
import app.notekea.core.logging.domain.FakeTextClipboard
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.FakeVectorIndex
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.TextFolder
import app.notekea.core.notes.domain.index.NoteChunk
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock

/** The chat panel over the real answer, with fakes of the index, the models and the history. */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val store = FakeNoteContentStore()
    private val index = FakeVectorIndex()
    private val embedder = FakeTextEmbedder()
    private val engine = FakeLlmEngine()
    private val runner = FakeActionRunner()
    private val clipboard = FakeTextClipboard()
    private val history = FakeChatHistoryStore()
    private val notes = mutableListOf<NoteEntry>()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
        engine.scoreOf = { _, _ -> 1f }
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `a question is answered with its sources, kept in the history, and the field empties`() =
        runTest(
            testDispatcher
        ) {
            indexed(
                path = AREPAS,
                body = AREPAS_BODY
            )

            engine.reply = { "Corn flour and warm water [1]." }
            val viewModel = started()

            viewModel.onIntent(intent = ChatIntent.OnQuestionChange(text = "What goes in the dough?"))
            viewModel.onIntent(intent = ChatIntent.OnSend)
            advanceUntilIdle()

            val entry = viewModel
                .state
                .value
                .entries
                .single()

            assertEquals(
                expected = listOf(TurnOutcome.ANSWERED, "Corn flour and warm water [1].", AREPAS, ""),
                actual = listOf(
                    entry.outcome,
                    entry.answer,
                    entry
                        .sources
                        .single()
                        .note,
                    viewModel.state.value.question
                )
            )

            assertEquals(
                expected = "What goes in the dough?",
                actual = history
                    .turns
                    .getValue(VAULT)
                    .single()
                    .question
            )
        }

    @Test
    fun `Copy chat puts each question, its answer and the passages it read on the clipboard`() =
        runTest(
            testDispatcher
        ) {
            indexed(
                path = AREPAS,
                body = AREPAS_BODY
            )

            engine.reply = { "Corn flour and warm water [1]." }
            val viewModel = started()

            viewModel.onIntent(intent = ChatIntent.OnQuestionChange(text = "What goes in the dough?"))
            viewModel.onIntent(intent = ChatIntent.OnSend)
            advanceUntilIdle()
            viewModel.onIntent(intent = ChatIntent.OnCopyChat)

            val copied = clipboard.copied.single()

            assertEquals(
                expected = listOf(true, true, true),
                actual = listOf(
                    "## What goes in the dough?" in copied,
                    "Corn flour and warm water [1]." in copied,
                    "[1] Recipes/Arepas.md: $AREPAS_BODY" in copied
                )
            )
        }

    @Test
    fun `without a model the chat cannot send, offers the settings, and looks again when shown`() =
        runTest(
            testDispatcher
        ) {
            engine.readiness = Outcome.Failure(error = LlmError.NoModel)
            val viewModel = started()
            viewModel.onIntent(intent = ChatIntent.OnQuestionChange(text = "Hi"))
            advanceUntilIdle()

            assertEquals(
                expected = ModelStatus.NO_MODEL to false,
                actual = viewModel.state.value.model to viewModel.state.value.canSend
            )

            viewModel.onIntent(intent = ChatIntent.OnModelSettingsOpen)

            assertEquals(
                expected = ChatEffect.OpenModelSettings,
                actual = viewModel.effects.first()
            )

            engine.readiness = Outcome.Success(value = LlmModelInfo(isSmall = true))
            viewModel.onIntent(intent = ChatIntent.OnShown)
            advanceUntilIdle()

            assertEquals(
                expected = ModelStatus.READY to true,
                actual = viewModel.state.value.model to viewModel.state.value.isSmall
            )
        }

    @Test
    fun `This note asks only the open note, and a citation opens its passage`() =
        runTest(testDispatcher) {
            indexed(
                path = AREPAS,
                body = AREPAS_BODY
            )

            indexed(
                path = BEANS,
                body = BEANS_BODY
            )

            engine.reply = { "Soak them overnight [1]." }
            val viewModel = started()

            viewModel.onIntent(intent = ChatIntent.OnActiveNoteChange(path = BEANS))
            viewModel.onIntent(intent = ChatIntent.OnScopeSelect(scope = ChatScope.NOTE))
            viewModel.onIntent(intent = ChatIntent.OnQuestionChange(text = "flour beans"))
            viewModel.onIntent(intent = ChatIntent.OnSend)
            advanceUntilIdle()

            val entry = viewModel
                .state
                .value
                .entries
                .single()

            assertEquals(
                expected = listOf(BEANS) to "Black beans",
                actual = entry.sources.map { it.note } to entry.scopeName
            )

            assertFalse(
                actual = engine
                    .generated
                    .single()[1]
                    .text
                    .contains(other = "corn")
            )

            viewModel.onIntent(intent = ChatIntent.OnSourceClick(source = entry.sources.single()))

            assertEquals(
                expected = ChatEffect.Reveal(
                    note = BEANS,
                    range = BEANS_BODY.indices
                ),
                actual = viewModel.effects.first()
            )
        }

    @Test
    fun `a failing model shows on the question and on the chat, and Retry asks again`() =
        runTest(testDispatcher) {
            indexed(
                path = AREPAS,
                body = AREPAS_BODY
            )

            val viewModel = started()
            engine.failure = LlmError.EngineFailed

            viewModel.onIntent(intent = ChatIntent.OnQuestionChange(text = "Flour?"))
            viewModel.onIntent(intent = ChatIntent.OnSend)
            advanceUntilIdle()

            val failed = viewModel
                .state
                .value
                .entries
                .single()

            assertEquals(
                expected = TurnOutcome.FAILED to ModelStatus.FAILED,
                actual = failed.outcome to viewModel.state.value.model
            )

            engine.failure = null
            engine.reply = { "Corn flour [1]." }
            viewModel.onIntent(intent = ChatIntent.OnRetry(id = failed.id))
            advanceUntilIdle()

            assertEquals(
                expected = listOf(TurnOutcome.ANSWERED),
                actual = viewModel
                    .state
                    .value
                    .entries
                    .map { it.outcome }
            )
        }

    @Test
    fun `the history comes back when the chat opens, and Clear history empties it`() =
        runTest(testDispatcher) {
            history.turns[VAULT] = listOf(
                ChatTurn(
                    question = "Old question",
                    scope = ChatScope.VAULT,
                    outcome = TurnOutcome.NOT_FOUND,
                    answer = "",
                    sources = emptyList(),
                    scopeName = null
                )
            )

            val viewModel = started()

            assertEquals(
                expected = listOf("Old question"),
                actual = viewModel
                    .state
                    .value
                    .entries
                    .map { it.question }
            )

            viewModel.onIntent(intent = ChatIntent.OnClearHistory)
            advanceUntilIdle()

            assertTrue(actual = viewModel.state.value.entries.isEmpty() && VAULT !in history.turns)
        }

    @Test
    fun `a request to write is proposed on a card, done with Do it and taken back with Undo`() =
        runTest(
            testDispatcher
        ) {
            engine.reply = { messages ->
                val isPlan = messages
                    .first()
                    .text
                    .startsWith(prefix = "You turn a request")

                if (isPlan) PLAN else "A short list of groceries."
            }

            val viewModel = started()

            viewModel.onIntent(intent = ChatIntent.OnQuestionChange(text = "create a note Groceries with a list"))
            viewModel.onIntent(intent = ChatIntent.OnSend)
            advanceUntilIdle()

            val proposed = viewModel
                .state
                .value
                .entries
                .last()

            val draft = proposed
                .action
                ?.steps
                ?.single()
                ?.content
                ?.single()

            viewModel.onIntent(intent = ChatIntent.OnActionConfirm(id = proposed.id))
            advanceUntilIdle()

            val done = viewModel
                .state
                .value
                .entries
                .last()
                .action
                ?.status

            viewModel.onIntent(intent = ChatIntent.OnActionUndo(id = proposed.id))
            advanceUntilIdle()

            assertEquals(
                expected = listOf<Any?>(
                    ActionStatus.PROPOSED,
                    "Groceries.md",
                    ActionStatus.DONE,
                    ActionStatus.UNDONE,
                    1
                ),
                actual = listOf(
                    proposed.action?.status,
                    draft?.path?.value,
                    done,
                    viewModel
                        .state
                        .value
                        .entries
                        .last()
                        .action
                        ?.status,
                    runner.undone.size
                )
            )
        }

    private fun TestScope.started(): ChatViewModel {
        val viewModel = chatViewModel(
            engine = engine,
            store = store,
            index = index,
            embedder = embedder,
            runner = runner,
            history = history,
            clipboard = clipboard
        )

        viewModel.onIntent(intent = ChatIntent.OnNotesChange(notes = notes))
        viewModel.onIntent(intent = ChatIntent.OnIndexChange(isReady = true))
        advanceUntilIdle()

        return viewModel
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
        const val PLAN =
            "{\"steps\":[{\"action\":\"create_note\",\"title\":\"Groceries\",\"text\":\"a list\"}]}"
        val AREPAS = RelativePath(value = "Recipes/Arepas.md")
        val BEANS = RelativePath(value = "Recipes/Black beans.md")
        const val AREPAS_BODY = "Mix corn flour with warm water and salt."
        const val BEANS_BODY = "Soak the beans overnight, then cook them with flour of plantain."
    }
}
