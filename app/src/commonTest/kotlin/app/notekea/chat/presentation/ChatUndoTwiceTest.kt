package app.notekea.chat.presentation

import app.notekea.chat.domain.FakeActionRunner
import app.notekea.chat.domain.FakeChatHistoryStore
import app.notekea.chat.presentation.model.ActionStatus
import app.notekea.chat.presentation.model.ChatIntent
import app.notekea.core.embedding.domain.FakeTextEmbedder
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.logging.domain.FakeTextClipboard
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.FakeVectorIndex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** A card's Undo clicked twice takes the plan back once (the code review of 2026-11-22). */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatUndoTwiceTest {
    private val dispatcher = StandardTestDispatcher()
    private val engine = FakeLlmEngine()
    private val runner = FakeActionRunner()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = dispatcher)
        engine.scoreOf = { _, _ -> 1f }

        engine.reply = { messages ->
            val isPlan = messages
                .first()
                .text
                .startsWith(prefix = "You turn a request")

            if (isPlan) PLAN else "A short list of groceries."
        }
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // The code review of 2026-11-22: a second Undo while the first ran undid the plan twice.
    @Test
    fun `Undo clicked twice undoes the plan once, and the card stays undone`() =
        runTest(dispatcher) {
            val viewModel = chatViewModel(
                engine = engine,
                store = FakeNoteContentStore(),
                index = FakeVectorIndex(),
                embedder = FakeTextEmbedder(),
                runner = runner,
                history = FakeChatHistoryStore(),
                clipboard = FakeTextClipboard()
            )

            viewModel.onIntent(intent = ChatIntent.OnNotesChange(notes = emptyList()))
            viewModel.onIntent(intent = ChatIntent.OnIndexChange(isReady = true))
            advanceUntilIdle()

            viewModel.onIntent(intent = ChatIntent.OnQuestionChange(text = "create a note Groceries with a list"))
            viewModel.onIntent(intent = ChatIntent.OnSend)
            advanceUntilIdle()

            val id = viewModel
                .state
                .value
                .entries
                .last()
                .id

            viewModel.onIntent(intent = ChatIntent.OnActionConfirm(id = id))
            advanceUntilIdle()

            viewModel.onIntent(intent = ChatIntent.OnActionUndo(id = id))
            viewModel.onIntent(intent = ChatIntent.OnActionUndo(id = id))
            advanceUntilIdle()

            assertEquals(
                expected = listOf<Any?>(1, ActionStatus.UNDONE),
                actual = listOf(
                    runner.undone.size,
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
        const val PLAN =
            "{\"steps\":[{\"action\":\"create_note\",\"title\":\"Groceries\",\"text\":\"a list\"}]}"
    }
}
