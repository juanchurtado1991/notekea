package app.notekea.notes.domain

import app.notekea.core.domain.markdown.CalloutFold
import app.notekea.core.domain.markdown.CalloutKind
import app.notekea.core.domain.markdown.CalloutParser
import app.notekea.core.domain.markdown.ReadingMarkdown
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The tasks and callouts cases of `MVP_SMOKE_TEST.md` (1.7) on the smoke note itself: every task and callout the
 * smoke guide promises is checked here against the file the Playground Vault gets.
 */
class SmokeTasksAndCalloutsTest {
    private val note = NoteDocument.fromText(text = File(SMOKE_NOTE).readText()).body

    @Test
    fun `1_7-01 the note has nine tasks, four of them checked, the capital X too`() {
        val tasks = TaskFinder().tasks(text = note)
        val checked = tasks.filter { it.isDone }.map { lineOf(index = it.stateIndex).substringAfter(delimiter = "] ") }

        assertEquals(
            expected = 9 to listOf(
                "Soak the beans",
                "Pasta e fagioli on Tuesday",
                "A numbered task done with a capital X",
                "A finished task inside a callout"
            ),
            actual = tasks.size to checked
        )
        assertEquals(
            expected = 0,
            actual = ReadingMarkdown().prepare(markdown = note).split('\n').count { it.contains(other = "[X]") }
        )
    }

    @Test
    fun `1_7-04 every callout of the note reads as the type, title and fold the guide states`() {
        val parser = CalloutParser()
        val headers = note.split('\n')
            .map { it.replace(regex = QUOTE_MARKS, replacement = "") }
            .mapNotNull { parser.header(line = it) }
            .map { Triple(it.kind, it.title, it.fold) }

        assertEquals(
            expected = listOf(
                Triple(CalloutKind.NOTE, "Note", CalloutFold.NONE),
                Triple(CalloutKind.ABSTRACT, "Abstract", CalloutFold.NONE),
                Triple(CalloutKind.INFO, "Info", CalloutFold.NONE),
                Triple(CalloutKind.TODO, "To do", CalloutFold.NONE),
                Triple(CalloutKind.TIP, "Tip", CalloutFold.NONE),
                Triple(CalloutKind.SUCCESS, "Success", CalloutFold.NONE),
                Triple(CalloutKind.QUESTION, "Question", CalloutFold.NONE),
                Triple(CalloutKind.WARNING, "Warning", CalloutFold.NONE),
                Triple(CalloutKind.FAILURE, "Failure", CalloutFold.NONE),
                Triple(CalloutKind.DANGER, "Danger", CalloutFold.NONE),
                Triple(CalloutKind.BUG, "Bug", CalloutFold.NONE),
                Triple(CalloutKind.EXAMPLE, "Example", CalloutFold.NONE),
                Triple(CalloutKind.QUOTE, "Quote", CalloutFold.NONE),
                Triple(CalloutKind.WARNING, "Caution is a warning", CalloutFold.NONE),
                Triple(CalloutKind.ABSTRACT, "TL;DR is an abstract", CalloutFold.NONE),
                Triple(CalloutKind.NOTE, "An unknown type is a note", CalloutFold.NONE),
                Triple(CalloutKind.QUESTION, "A folded question (click to open)", CalloutFold.COLLAPSED),
                Triple(CalloutKind.TIP, "An open tip (click to fold)", CalloutFold.EXPANDED),
                Triple(CalloutKind.INFO, "Level one", CalloutFold.NONE),
                Triple(CalloutKind.WARNING, "Level two", CalloutFold.NONE),
                Triple(CalloutKind.DANGER, "Level three", CalloutFold.NONE)
            ),
            actual = headers
        )
    }

    private fun lineOf(
        index: Int
    ): String {
        val start = note.lastIndexOf(char = '\n', startIndex = index) + 1
        val end = note.indexOf(char = '\n', startIndex = index).takeIf { it >= 0 } ?: note.length
        return note.substring(startIndex = start, endIndex = end)
    }

    companion object {
        private const val SMOKE_NOTE = "../smoke-data/notes/Tasks and callouts.md"
        private val QUOTE_MARKS = Regex(pattern = "^(?:\\s*>\\s?)+")
    }
}
