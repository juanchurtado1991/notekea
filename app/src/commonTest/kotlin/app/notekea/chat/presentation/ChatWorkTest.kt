package app.notekea.chat.presentation

import app.notekea.chat.domain.FakeActionRunner
import app.notekea.chat.domain.FakeChatHistoryStore
import app.notekea.chat.domain.context.WorkContext
import app.notekea.chat.domain.context.WorkPlace
import app.notekea.chat.presentation.model.ChatIntent
import app.notekea.chat.presentation.model.WorkPart
import app.notekea.chat.presentation.viewmodel.ChatViewModel
import app.notekea.core.embedding.domain.FakeTextEmbedder
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.logging.domain.FakeTextClipboard
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.FakeVectorIndex
import app.notekea.core.notes.domain.NoteFixtures.entry
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

/** The chat knows where the person is (the developer's choice of 2026-11-13: always, shown as chips). */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatWorkTest {
    private val dispatcher = StandardTestDispatcher()
    private val engine = FakeLlmEngine()

    private val arepas = WorkContext(
        place = WorkPlace.NOTE,
        name = "Recipes/Arepas",
        section = "Ingredients",
        selected = "2 1/2 cups warm water"
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher = dispatcher)
        engine.reply = { TALK }
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `where the person is and what they selected reach the plan and the talk, as one line`() =
        runTest(dispatcher) {
            val viewModel = started()

            viewModel.onIntent(intent = ChatIntent.OnWorkChange(working = arepas))
            asked(viewModel = viewModel)

            val told = engine.generated.map { messages -> messages.last().text }

            assertEquals(
                expected = listOf(true, true),
                actual = listOf(
                    told.first().contains(LINE),
                    told.last().contains(LINE)
                )
            )
        }

    @Test
    fun `a removed chip stays out of the model until the person moves on`() = runTest(dispatcher) {
        val viewModel = started()

        viewModel.onIntent(intent = ChatIntent.OnWorkChange(working = arepas))
        viewModel.onIntent(intent = ChatIntent.OnWorkRemove(part = WorkPart.SELECTED))
        val withoutSelected = viewModel.state.value.shared

        viewModel.onIntent(intent = ChatIntent.OnWorkRemove(part = WorkPart.PLACE))
        viewModel.onIntent(intent = ChatIntent.OnWorkChange(working = arepas.copy(section = "Steps")))
        val stillHidden = viewModel.state.value.shared

        val graph = WorkContext(place = WorkPlace.GRAPH)
        viewModel.onIntent(intent = ChatIntent.OnWorkChange(working = graph))
        advanceUntilIdle()

        assertEquals(
            expected = listOf<Any?>(arepas.copy(selected = null), null, graph),
            actual = listOf(withoutSelected, stillHidden, viewModel.state.value.shared)
        )
    }

    @Test
    fun `the context says the place, its section and the selection`() {
        assertEquals(
            expected = listOf(
                LINE + " They selected this text: \"2 1/2 cups warm water\".",
                "The person is in the graph view."
            ),
            actual = listOf(
                arepas.described(),
                WorkContext(place = WorkPlace.GRAPH).described()
            )
        )
    }

    private fun TestScope.started(): ChatViewModel {
        val viewModel = chatViewModel(
            engine = engine,
            store = FakeNoteContentStore(),
            index = FakeVectorIndex(),
            embedder = FakeTextEmbedder(),
            runner = FakeActionRunner(),
            history = FakeChatHistoryStore(),
            clipboard = FakeTextClipboard()
        )

        viewModel.onIntent(intent = ChatIntent.OnNotesChange(notes = listOf(entry(path = "Recipes/Arepas.md"))))
        viewModel.onIntent(intent = ChatIntent.OnIndexChange(isReady = true))
        advanceUntilIdle()

        return viewModel
    }

    private fun TestScope.asked(viewModel: ChatViewModel) {
        viewModel.onIntent(intent = ChatIntent.OnQuestionChange(text = "what do you think of this?"))
        viewModel.onIntent(intent = ChatIntent.OnSend)
        advanceUntilIdle()
    }

    private companion object {
        const val TALK = "{\"steps\":[{\"action\":\"chat\"}]}"
        const val LINE = "The person is in the note Recipes/Arepas, at the section \"Ingredients\"."
    }
}
