package app.notekea.editor.presentation.view

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextRange
import app.notekea.editor.domain.TableAction
import app.notekea.editor.domain.TableCell
import app.notekea.editor.domain.TableText
import app.notekea.editor.presentation.model.EditorIntent

/**
 * The keys of a table's cell, while the text field is over one: Tab and Enter go to the next cell and to the one below
 * (a new row after the last), the arrows cross to the neighbour cells and out of the table, and Backspace and Delete
 * stop at the cell's edges, where they would delete a pipe.
 */
internal class CellKeys(
    private val page: EditorPageState,
    private val onIntent: (EditorIntent) -> Unit
) {
    /** False for a key that is not the table's, so the next one handles it. */
    fun onKey(event: KeyEvent): Boolean {
        val table = page.table
        val cell = page.cell

        val isIgnored =
            event.type != KeyEventType.KeyDown ||
                event.isCtrlPressed ||
                event.isMetaPressed ||
                event.isAltPressed

        if (table == null ||
            cell == null ||
            isIgnored
        ) {
            return false
        }

        val selection = page.buffer.selection
        val isPlain = !event.isShiftPressed

        val isAtStart =
            isPlain &&
                selection.collapsed &&
                selection.start == cell.start

        val isAtEnd =
            isPlain &&
                selection.collapsed &&
                selection.start == cell.end

        val above = table.cell(
            row = cell.row - 1,
            column = cell.column
        )

        val below = table.cell(
            row = cell.row + 1,
            column = cell.column
        )

        val previous = table.cell(
            row = cell.row,
            column = cell.column - 1
        ) ?: table
            .rows
            .getOrNull(index = cell.row - 1)
            ?.last()

        val next = table.cell(
            row = cell.row,
            column = cell.column + 1
        ) ?: table
            .rows
            .getOrNull(index = cell.row + 1)
            ?.first()

        return when (event.key) {
            Key.Tab -> if (isPlain) {
                go(to = next) || edit(action = TableAction.NEXT_ROW)
            } else {
                go(to = previous ?: cell)
            }

            Key.Enter, Key.NumPadEnter -> go(to = below) || edit(action = TableAction.ROW_BELOW)

            Key.DirectionUp -> isPlain && (go(to = above) || leaveUp(table = table))

            Key.DirectionDown -> isPlain && (go(to = below) || leaveDown(table = table))

            Key.DirectionLeft -> isAtStart && (go(to = previous) || leaveUp(table = table))

            Key.DirectionRight -> isAtEnd && (go(offset = next?.start) || leaveDown(table = table))

            // At the cell's edges they would delete its pipe.
            Key.Backspace -> isAtStart

            Key.Delete -> isAtEnd

            else -> false
        }
    }

    private fun go(to: TableCell?): Boolean = go(offset = to?.end)

    /** Whether the cursor went to [offset]: not when there is none. */
    private fun go(offset: Int?): Boolean {
        if (offset == null) return false

        page.buffer.edit { selection = TextRange(index = offset) }

        return true
    }

    private fun edit(action: TableAction): Boolean {
        onIntent(EditorIntent.OnTableEdit(action = action))

        return true
    }

    /** Out of the table to the line over it, if the note has one. */
    private fun leaveUp(table: TableText): Boolean {
        go(
            offset = (table.start - 1).takeIf {
                it >= 0
            }
        )

        return true
    }

    /** Out of the table to the text under it; a table that ends the note gets a line to write on. */
    private fun leaveDown(table: TableText): Boolean {
        val nextBlock = (page.items.shown.max + 1).takeIf {
            it <= page.buffer.text.length
        }

        return go(offset = table.afterStart ?: nextBlock) || edit(action = TableAction.AFTER)
    }
}
