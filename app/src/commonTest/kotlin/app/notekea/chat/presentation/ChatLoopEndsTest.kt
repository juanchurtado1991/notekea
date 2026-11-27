package app.notekea.chat.presentation

import app.notekea.chat.domain.FakeActionRunner
import app.notekea.chat.domain.FakeChatHistoryStore
import app.notekea.chat.domain.loop.Toolsets
import app.notekea.chat.domain.loop.WorkKind
import app.notekea.chat.domain.plan.FoundNotes
import app.notekea.chat.domain.plan.FoundRow
import app.notekea.chat.domain.tool.lookup.FindTool
import app.notekea.chat.domain.tool.lookup.ReadTool
import app.notekea.chat.domain.tool.work.AnswerTool
import app.notekea.chat.presentation.model.ChatEffect
import app.notekea.chat.presentation.model.ChatIntent
import app.notekea.chat.presentation.viewmodel.ChatViewModel
import app.notekea.core.embedding.domain.FakeTextEmbedder
import app.notekea.core.llm.domain.FakeLlmEngine
import app.notekea.core.logging.domain.FakeTextClipboard
import app.notekea.core.notes.domain.FakeNoteContentStore
import app.notekea.core.notes.domain.FakeVectorIndex
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteTextCache
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.ViewModelTestBase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The chat shows a loop's answer as the planner's answers were shown (`docs/agent-loop.md`, "Step 6, finished"): the
 * notes it found as a table under the answer, and a note it read whole, among its sources, opened by a click.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatLoopEndsTest : ViewModelTestBase() {
    private val store = FakeNoteContentStore()
    private val engine = FakeLlmEngine()
    private val runner = FakeActionRunner()
    private val arepas = RelativePath(value = "Recipes/Arepas.md")

    private val table = FoundNotes(
        columns = listOf("Note"),
        rows = listOf(FoundRow(note = arepas))
    )

    private fun TestScope.asked(
        tool: String,
        question: String
    ): ChatViewModelAsked {
        var calls = 0

        engine.reply = { messages ->
            val asked = messages.first().text

            when {
                asked.startsWith(prefix = "You say how a request") -> """{"work":"ask","skill":"none"}"""
                asked.startsWith(prefix = CALLS) && calls++ == 0 -> tool
                asked.startsWith(prefix = CALLS) -> """{"tool":"answer","text":"Arepas."}"""
                asked.startsWith(prefix = "Write the answer") -> ANSWER
                else -> ""
            }
        }

        val cache = NoteTextCache(contentStore = store)

        val viewModel = chatViewModel(
            engine = engine,
            store = store,
            index = FakeVectorIndex(),
            embedder = FakeTextEmbedder(),
            runner = runner,
            history = FakeChatHistoryStore(),
            clipboard = FakeTextClipboard(),
            toolsets = Toolsets(
                byKind = mapOf(
                    WorkKind.ASK to listOf(
                        FindTool(actionRunner = runner),
                        ReadTool(noteTextCache = cache),
                        AnswerTool()
                    )
                )
            )
        )

        val notes = listOf(entry(path = arepas.value))

        viewModel.onIntent(intent = ChatIntent.OnNotesChange(notes = notes))
        viewModel.onIntent(intent = ChatIntent.OnIndexChange(isReady = true))
        advanceUntilIdle()

        viewModel.onIntent(intent = ChatIntent.OnQuestionChange(text = question))
        viewModel.onIntent(intent = ChatIntent.OnSend)
        advanceUntilIdle()

        return ChatViewModelAsked(viewModel)
    }

    @Test
    fun `the notes a turn found show as a table under its answer`() = runTest(testDispatcher) {
        runner.found = table

        val entry = asked(
            tool = """{"tool":"find","query":"recipes with corn"}""",
            question = "which recipes use corn?"
        ).entry

        assertEquals(
            expected = listOf<Any?>(ANSWER, listOf(table)),
            actual = listOf(entry.answer, entry.action?.found)
        )
    }

    @Test
    fun `a note the answer read whole is a source that opens the note`() = runTest(testDispatcher) {
        store.texts[arepas] = "Mix 2½ cups of warm water with the flour."

        val asked = asked(
            tool = """{"tool":"read","note":"Arepas"}""",
            question = "how much water do the arepas take?"
        )

        asked.viewModel.onIntent(
            intent = ChatIntent.OnSourceClick(
                source = asked
                    .entry
                    .sources
                    .single()
            )
        )

        assertEquals(
            expected = ChatEffect.Open(note = arepas),
            actual = asked
                .viewModel
                .effects
                .first()
        )
    }

    private companion object {
        const val CALLS = "You work on a task in the person's notes"
        const val ANSWER = "The Arepas note is the one."
    }
}

// The chat after one question, with that question's entry.
private class ChatViewModelAsked(val viewModel: ChatViewModel) {
    val entry get() = viewModel
        .state
        .value
        .entries
        .single()
}
