package app.notekea.editor.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class ListLineTest {
    private val editing = ListEditing()

    @Test
    fun `a line holding a character Regex takes for a line end is still read, whole`() {
        assertEquals(
            expected = LINE_ENDS.map {
                "- " to "a${it}b"
            },
            actual = LINE_ENDS.map { char ->
                ListLine.of(line = "- a${char}b").let {
                    it.marker to it.content
                }
            }
        )
    }

    @Test
    fun `a line ending in such a character keeps it`() {
        assertEquals(
            expected = LINE_ENDS.map { "a$it" },
            actual = LINE_ENDS.map { ListLine.of(line = "- a$it").content }
        )
    }

    @Test
    fun `every list key works on a line holding such a character`() {
        LINE_ENDS.forEach { char ->
            val text = "- a\n- b${char}c"
            val end = text.length

            assertEquals(
                expected = "- a\n- b${char}c\n- ",
                actual = applied(
                    text = text,
                    cursor = end,
                    action = ListAction.NEW_ITEM
                )
            )

            assertEquals(
                expected = "- a\n  - b${char}c",
                actual = applied(
                    text = text,
                    cursor = end,
                    action = ListAction.INDENT
                )
            )

            assertEquals(
                expected = "- a\nb${char}c",
                actual = applied(
                    text = text,
                    cursor = text.indexOf(char = 'b'),
                    action = ListAction.REMOVE_MARKER
                )
            )
        }
    }

    private fun applied(
        text: String,
        cursor: Int,
        action: ListAction
    ): String {
        val edit = requireNotNull(
            editing.edit(
                text = text,
                selectionStart = cursor,
                selectionEnd = cursor,
                action = action
            )
        )

        return text.replaceRange(
            startIndex = edit.start,
            endIndex = edit.end,
            replacement = edit.replacement
        )
    }

    private companion object {
        val LINE_ENDS = listOf(' ', ' ', '\u0085', '\r')
    }
}
