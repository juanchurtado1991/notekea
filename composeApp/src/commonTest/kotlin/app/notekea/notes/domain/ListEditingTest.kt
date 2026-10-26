package app.notekea.notes.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class ListEditingTest {
    private val editing = ListEditing()

    @Test
    fun `enter continues bullets, numbers, tasks and quotes, carrying the text after the cursor`() {
        assertEquals(
            expected = listOf(
                "- Flour\n- |",
                "1. Flour\n2. |",
                "- [x] Flour\n- [ ] |",
                "  * Flour\n  * |",
                "> [!tip] Tip\n> |",
                "> - Flour\n> - |",
                "- Corn\n- |flour"
            ),
            actual = listOf(
                "- Flour|",
                "1. Flour|",
                "- [x] Flour|",
                "  * Flour|",
                "> [!tip] Tip|",
                "> - Flour|",
                "- Corn|flour"
            ).map { apply(marked = it, action = ListAction.NEW_ITEM) }
        )
    }

    @Test
    fun `enter on an empty item ends the list, and keeps its usual meaning elsewhere`() {
        assertEquals(
            expected = listOf<String?>("- Flour\n|", "1. Flour\n|", null, null, null),
            actual = listOf(
                "- Flour\n- |",
                "1. Flour\n2. |",
                "Plain text|",
                "|- Flour",
                "```\n- inside code|\n```"
            ).map { applyOrNull(marked = it, action = ListAction.NEW_ITEM) }
        )
    }

    @Test
    fun `tab nests an item under the text of the item above, shift tab takes it back to its parent's level`() {
        assertEquals(
            expected = listOf(
                "- Cook\n  - Arepas|",
                "1. Cook\n   1. Arepas|",
                "- [ ] Cook\n  - [ ] Arepas|",
                "- Cook\n- Arepas|",
                "- Cook\n  - Arepas\n  - Soak|"
            ),
            actual = listOf(
                apply(marked = "- Cook\n- Arepas|", action = ListAction.INDENT),
                apply(marked = "1. Cook\n1. Arepas|", action = ListAction.INDENT),
                apply(marked = "- [ ] Cook\n- [ ] Arepas|", action = ListAction.INDENT),
                apply(marked = "- Cook\n  - Arepas|", action = ListAction.OUTDENT),
                apply(marked = "- Cook\n  - Arepas\n    - Soak|", action = ListAction.OUTDENT)
            )
        )
    }

    @Test
    fun `tab on several selected items moves them together, and on the first item or plain text changes nothing`() {
        assertEquals(
            expected = listOf<String?>("- Cook\n  - [Arepas\n  - Pasta]", "- First|", null),
            actual = listOf(
                applyOrNull(marked = "- Cook\n- [Arepas\n- Pasta]", action = ListAction.INDENT),
                applyOrNull(marked = "- First|", action = ListAction.INDENT),
                applyOrNull(marked = "Plain|", action = ListAction.INDENT)
            )
        )
    }

    @Test
    fun `backspace right after a marker removes it, elsewhere it deletes as usual`() {
        assertEquals(
            expected = listOf<String?>("|Flour", "  |", "> |", null, null),
            actual = listOf(
                applyOrNull(marked = "- [ ] |Flour", action = ListAction.REMOVE_MARKER),
                applyOrNull(marked = "  1. |", action = ListAction.REMOVE_MARKER),
                applyOrNull(marked = "> - |", action = ListAction.REMOVE_MARKER),
                applyOrNull(marked = "- Fl|our", action = ListAction.REMOVE_MARKER),
                applyOrNull(marked = "> |quote", action = ListAction.REMOVE_MARKER)
            )
        )
    }

    private fun apply(
        marked: String,
        action: ListAction
    ): String = applyOrNull(marked = marked, action = action) ?: error("No edit for $marked")

    /**
     * The text after [action] on [marked], where `|` is the cursor or `[` … `]` the selection, written back the same
     * way; null when the key keeps its usual meaning.
     */
    private fun applyOrNull(
        marked: String,
        action: ListAction
    ): String? {
        val (text, start, end) = unmark(marked = marked)
        val edit = editing.edit(text = text, selectionStart = start, selectionEnd = end, action = action) ?: return null
        val result = text.replaceRange(startIndex = edit.start, endIndex = edit.end, replacement = edit.replacement)
        return if (edit.selectionStart == edit.selectionEnd) {
            result.substring(startIndex = 0, endIndex = edit.selectionStart) + "|" +
                result.substring(startIndex = edit.selectionStart)
        } else {
            result.substring(startIndex = 0, endIndex = edit.selectionStart) + "[" +
                result.substring(startIndex = edit.selectionStart, endIndex = edit.selectionEnd) + "]" +
                result.substring(startIndex = edit.selectionEnd)
        }
    }

    private fun unmark(
        marked: String
    ): Triple<String, Int, Int> {
        val cursor = marked.indexOf(char = '|')
        if (cursor >= 0) return Triple(marked.removeRange(range = cursor..cursor), cursor, cursor)
        val start = marked.indexOf(char = '[', startIndex = marked.indexOf(char = '\n').coerceAtLeast(minimumValue = 0))
        val end = marked.lastIndexOf(char = ']')
        val text = marked.removeRange(range = end..end).removeRange(range = start..start)
        return Triple(text, start, end - 1)
    }
}
