package app.notekea.core.presentation.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect

/**
 * Where a [NotekeaTabStrip]'s [row] and each of its tabs ([places]) are in the window, to tell a tab dropped on the row
 * (a new place among the others) from one dropped elsewhere (another pane).
 */
internal class TabDropTargets {
    var row: Rect = Rect.Zero
    val places = mutableMapOf<Int, Rect>()

    /**
     * The tab at [index] let go at [position]: on the row it goes to [onReorder] with its new index among the other
     * [count] - 1 tabs (nothing when it stays in its place); anywhere else to [onDrop].
     */
    fun drop(
        index: Int,
        position: Offset,
        count: Int,
        onReorder: (from: Int, to: Int) -> Unit,
        onDrop: (index: Int, windowPosition: Offset) -> Unit
    ) {
        if (!row.contains(offset = position)) {
            onDrop(index, position)
            return
        }
        val before = places.filterKeys { it != index && it < count }.values.count { it.center.x < position.x }
        if (before != index) onReorder(index, before)
    }
}
