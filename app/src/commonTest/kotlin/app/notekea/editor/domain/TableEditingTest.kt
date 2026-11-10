package app.notekea.editor.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class TableEditingTest {
    private val editing = TableEditing()

    @Test
    fun `rows are added around the cursor's row, and tab in the last cell starts a new one`() {
        assertEquals(
            expected = listOf(
                "$HEAD\n|  | 2 |\n| ^ |  |\n| Corn | 3 |",
                "| ^ |  |\n| --- | --: |\n| Item | Qty |\n| Corn | 3 |",
                "$HEAD\n| Corn | 3 |\n|  | ^ |",
                "$HEAD\n| Corn | 3 |\n| ^ |  |"
            ),
            actual = listOf(
                "$HEAD\n|^ | 2 |\n| Corn | 3 |" to TableAction.ROW_BELOW,
                "| It^em | Qty |\n| --- | --: |\n| Corn | 3 |" to TableAction.ROW_ABOVE,
                "$HEAD\n| Corn | 3^ |" to TableAction.ROW_BELOW,
                "$HEAD\n| Corn | 3^ |" to TableAction.NEXT_ROW
            ).map { (marked, action) ->
                apply(
                    marked = marked,
                    action = action
                )
            }
        )
    }

    @Test
    fun `columns are added beside the cursor's column, with no alignment`() {
        assertEquals(
            expected = listOf(
                "| Item | ^ | Qty |\n| --- | --- | --: |\n| Corn |  | 3 |",
                "| Item |  | Qty |\n| --- | --- | --: |\n| Corn | ^ | 3 |"
            ),
            actual = listOf(
                "| It^em | Qty |\n| --- | --: |\n| Corn | 3 |" to TableAction.COLUMN_RIGHT,
                "$HEAD\n| Corn | ^3 |" to TableAction.COLUMN_LEFT
            ).map { (marked, action) ->
                apply(
                    marked = marked,
                    action = action
                )
            }
        )
    }

    @Test
    fun `an alignment is written in the delimiter row, and chosen again it is taken away`() {
        assertEquals(
            expected = listOf(
                "| Item | Qty |\n| :-: | --: |\n| Corn^ | 3 |",
                "| Item | Qty |\n| --- | :-- |\n| Corn | 3^ |",
                "| Item | Qty |\n| --- | --- |\n| Corn | 3^ |"
            ),
            actual = listOf(
                "$HEAD\n| Co^rn | 3 |" to TableAction.ALIGN_CENTER,
                "$HEAD\n| Corn | ^3 |" to TableAction.ALIGN_LEFT,
                "$HEAD\n| Corn | ^3 |" to TableAction.ALIGN_RIGHT
            ).map { (marked, action) ->
                apply(
                    marked = marked,
                    action = action
                )
            }
        )
    }

    @Test
    fun `deleting a row or a column keeps the cursor in the table, and the last one takes the table away`() {
        assertEquals(
            expected = listOf(
                "$HEAD\n| Rice^ | 1 |",
                "| Corn^ | 3 |\n| --- | --: |",
                "| Qty^ |\n| --: |\n| 3 |",
                "| Item |\n| --- |\n| Corn^ |",
                "Before\n\n^\n\nAfter",
                "^"
            ),
            actual = listOf(
                "$HEAD\n| Co^rn | 3 |\n| Rice | 1 |" to TableAction.DELETE_ROW,
                "| It^em | Qty |\n| --- | --: |\n| Corn | 3 |" to TableAction.DELETE_ROW,
                "| It^em | Qty |\n| --- | --: |\n| Corn | 3 |" to TableAction.DELETE_COLUMN,
                "$HEAD\n| Corn | ^3 |" to TableAction.DELETE_COLUMN,
                "Before\n\n| It^em |\n| --- |\n\nAfter" to TableAction.DELETE_ROW,
                "| It^em |\n| --- |\n| Corn |" to TableAction.DELETE_COLUMN
            ).map { (marked, action) ->
                apply(
                    marked = marked,
                    action = action
                )
            }
        )
    }

    @Test
    fun `a change to the rows writes every cell with one space around it and keeps the text around the table`() {
        assertEquals(
            expected = "# Title\n\n| a | b\\|c |\n| :-- | --- |\n| ^ |  |\n| d |  |\n\nAfter",
            actual = apply(
                marked = "# Title\n\n|a^|   b\\|c   |\n|:---|-|\n|d||\n\nAfter",
                action = TableAction.ROW_BELOW
            )
        )
    }

    @Test
    fun `leaving a table downwards gives the note a blank line under it, once`() {
        assertEquals(
            expected = listOf("$HEAD\n\n^", "$HEAD\n\n^", "$HEAD\n\n^"),
            actual = listOf("| It^em | Qty |\n| --- | --: |", "$HEAD^\n", "$HEAD\n\n^").map {
                apply(
                    marked = it,
                    action = TableAction.AFTER
                )
            }
        )
    }

    @Test
    fun `outside a table that can be edited by cells there is nothing to do`() {
        assertEquals(
            expected = listOf<String?>(null, null, null),
            actual = listOf(
                "Plain ^text",
                "| a | b |\n| --- | --- |\n| ^c |",
                "$HEAD\n\nTe^xt under it"
            ).map {
                applyOrNull(
                    marked = it,
                    action = TableAction.ROW_BELOW
                )
            }
        )
    }

    private fun apply(
        marked: String,
        action: TableAction
    ): String = applyOrNull(
        marked = marked,
        action = action
    ) ?: error("No edit for $marked")

    /** The text after [action] on [marked], where `^` is the cursor, written back the same way; null for no edit. */
    private fun applyOrNull(
        marked: String,
        action: TableAction
    ): String? {
        val cursor = marked.indexOf(char = CURSOR)

        val text = marked.replace(
            oldValue = CURSOR.toString(),
            newValue = ""
        )

        val edit = editing.edit(
            text = text,
            cursor = cursor,
            action = action
        ) ?: return null

        val result = text.replaceRange(
            startIndex = edit.start,
            endIndex = edit.end,
            replacement = edit.replacement
        )

        return result.substring(
            startIndex = 0,
            endIndex = edit.selectionStart
        ) + CURSOR +
            result.substring(startIndex = edit.selectionStart)
    }

    companion object {
        private const val CURSOR = '^'
        private const val HEAD = "| Item | Qty |\n| --- | --: |"
    }
}
