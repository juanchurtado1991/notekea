package app.notekea.editor.presentation.view

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import app.notekea.editor.domain.TableCell

/**
 * Where the drawn table has the cell being edited, inside the list's item ([container]) that lays the text field
 * over it: known once the table is laid out, a frame after the cursor enters the cell.
 */
@Stable
internal class CellPlace {
    var container: LayoutCoordinates? = null

    private var placed: Placed? by mutableStateOf(value = null)

    /** The place of [cell]; null while the last one known is another cell's. */
    fun of(cell: TableCell): Rect? = placed
        ?.takeIf {
            it.row == cell.row && it.column == cell.column
        }
        ?.rect

    /** For the drawn cell at [row] and [column], the one being edited: tells its place when it changes. */
    fun watch(
        row: Int,
        column: Int
    ): Modifier = Modifier.onGloballyPositioned { cell ->
        val rect = container
            ?.takeIf { it.isAttached }
            ?.localBoundingBoxOf(
                sourceCoordinates = cell,
                clipBounds = false
            )

        if (rect != null) {
            placed = Placed(
                row = row,
                column = column,
                rect = rect
            )
        }
    }

    private data class Placed(
        val row: Int,
        val column: Int,
        val rect: Rect
    )
}
