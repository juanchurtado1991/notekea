package app.notekea.chat.presentation

import app.notekea.chat.domain.ChatScope
import app.notekea.chat.domain.FakeActionRunner
import app.notekea.chat.domain.FakeChatHistoryStore
import app.notekea.chat.domain.TurnOutcome
import app.notekea.chat.domain.help.HelpGuide
import app.notekea.chat.presentation.model.ChatEntry
import app.notekea.chat.presentation.model.ChatIntent
import app.notekea.chat.presentation.viewmodel.ChatViewModel
import app.notekea.core.embedding.domain.FakeTextEmbedder
import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.logging.domain.FakeTextClipboard
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.FakeVectorIndex
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.presentation.ViewModelTestBase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The chat's smoke of 2026-11-11: "how do I create a note?" is explained with the real shortcut and never creates a
 * note, "what is in my notes?" describes the vault, and "en español" says the last reply again.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatHelpTest : ViewModelTestBase() {
    private val engine = FakeLlmEngine()
    private val runner = FakeActionRunner()
    private var route = ""

    private val guide = HelpGuide.of(
        markdown = "# Create a note\nwords: create a note, nueva nota\ntry: create a note called …\n" +
            "Press {newNote} to create a note."
    )

    @BeforeTest
    fun setUp() {
        engine.reply = { messages ->
            when {
                messages.isPrompt(start = "You say how a request") -> route

                messages.isPrompt(
                    start = "You are the assistant of Notekea, a notes app that runs on the user's " +
                        "computer. Explain"
                ) -> "Pulsa Ctrl+N."

                messages.isPrompt(start = "You are the assistant of Notekea, a notes app. Say") -> "Recetas y viajes."

                else -> "Las notas tienen recetas."
            }
        }
    }

    private fun List<ChatMessage>.isPrompt(start: String): Boolean =
        first().text.startsWith(prefix = start)

    private fun TestScope.asked(
        viewModel: ChatViewModel,
        question: String
    ): ChatEntry {
        viewModel.onIntent(intent = ChatIntent.OnQuestionChange(text = question))
        viewModel.onIntent(intent = ChatIntent.OnSend)
        advanceUntilIdle()

        return viewModel
            .state
            .value
            .entries
            .last()
    }

    private fun TestScope.started(): ChatViewModel {
        val viewModel = chatViewModel(
            engine = engine,
            store = FakeNoteContentStore(),
            index = FakeVectorIndex(),
            embedder = FakeTextEmbedder(),
            runner = runner,
            history = FakeChatHistoryStore(),
            clipboard = FakeTextClipboard(),
            guide = guide
        )

        viewModel.onIntent(
            intent = ChatIntent.OnNotesChange(
                notes = listOf(entry(path = "Recipes/Arepas.md"), entry(path = "Travel/Lisbon trip.md"))
            )
        )

        viewModel.onIntent(intent = ChatIntent.OnIndexChange(isReady = true))
        advanceUntilIdle()

        return viewModel
    }

    @Test
    fun `how to create a note is explained with this system's shortcut, offered, and never done`() =
        runTest(testDispatcher) {
            val viewModel = started()

            // Help is the chat's own reply: nothing is proposed.
            route = """{"work":"help","skill":"none"}"""

            val entry = asked(
                viewModel = viewModel,
                question = "¿cómo creo una nota nueva?"
            )

            val helpPrompt = engine
                .generated
                .last()
                .first()
                .text

            viewModel.onIntent(intent = ChatIntent.OnOfferedClick(id = entry.id))

            assertEquals(
                expected = listOf<Any?>("Pulsa Ctrl+N.", TurnOutcome.ANSWERED, null, true, "create a note called …"),
                actual = listOf(
                    entry.answer,
                    entry.outcome,
                    entry.action,
                    "Press Ctrl+N to create a note." in helpPrompt,
                    viewModel.state.value.question
                )
            )
        }

    @Test
    fun `what is in the notes is told from the vault's folders and names`() =
        runTest(testDispatcher) {
            val viewModel = started()
            route = """{"work":"overview","skill":"none"}"""

            val entry = asked(
                viewModel = viewModel,
                question = "explicame que hay en las notas"
            )

            val vault = engine
                .generated
                .last()
                .first()
                .text

            assertEquals(
                expected = listOf<Any?>("Recetas y viajes.", true, true),
                actual = listOf(entry.answer, "- Recipes (1): Arepas" in vault, "- Travel (1): Lisbon trip" in vault)
            )
        }

    @Test
    fun `in Spanish says the last reply again from the chat so far`() = runTest(testDispatcher) {
        val viewModel = started()
        route = """{"work":"overview","skill":"none"}"""

        asked(
            viewModel = viewModel,
            question = "what is in my notes?"
        )

        route = """{"work":"talk","skill":"none"}"""

        val again = asked(
            viewModel = viewModel,
            question = "en español"
        )

        val talk = engine.generated.last()

        val expected =
            listOf<Any?>("Las notas tienen recetas.", "Recetas y viajes.", ChatScope.VAULT)

        assertEquals(
            expected = expected,
            actual = listOf(again.answer, talk[talk.size - 2].text, again.scope)
        )
    }
}
