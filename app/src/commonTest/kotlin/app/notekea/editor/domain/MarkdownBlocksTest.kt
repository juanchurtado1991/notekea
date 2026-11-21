package app.notekea.editor.domain

import kotlin.test.Test
import kotlin.test.assertEquals

/** The blocks the toolbar inserts, around a selection. */
class MarkdownBlocksTest {
    private val formatter = MarkdownFormatter()

    @Test
    fun `a table or a divider inserted with a selection goes after it and keeps the selected text`() {
        val text = "Keep this"

        val results = listOf(
            FormatAction.Table(
                columnNames = listOf("A"),
                rows = 1,
                cellText = "Text"
            ),
            FormatAction.Divider
        ).map { action ->
            val edit = formatter.format(
                text = text,
                selectionStart = 0,
                selectionEnd = text.length,
                action = action
            )

            text.replaceRange(
                startIndex = edit.start,
                endIndex = edit.end,
                replacement = edit.replacement
            )
        }

        assertEquals(
            expected = listOf(
                "Keep this\n\n| A |\n| --- |\n| Text |\n",
                "Keep this\n\n---\n\n"
            ),
            actual = results
        )
    }
}
