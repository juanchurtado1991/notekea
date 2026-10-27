package app.notekea.core.presentation.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect

/**
 * Where a [NotekeaTabStrip]'s [row] and each of its tabs ([places]) are in the window, and the tab being dragged
 * ([drag]), to slide the other tabs aside while it moves along the row, keep it on the row's line there, and tell a tab
 * dropped on the row (a new place among the others) from one dropped elsewhere (another pane).
 */
internal class TabDropTargets {
    var row: Rect = Rect.Zero
    val places = mutableMapOf<Int, Rect>()
    var drag by mutableStateOf<TabDrag?>(value = null)

    /** The drag that still moves the tabs of [keys]: none once they took the new order it asked for. */
    fun dragOf(keys: List<Any>): TabDrag? = drag?.takeIf { it.keys == keys }

    /** The place among the [TabDrag.keys] the dragged tab takes if let go now (its own when off the row) or took. */
    fun slotOf(drag: TabDrag): Int {
        drag.landing?.let { return it }
        if (!row.contains(offset = drag.pointer)) return drag.index
        return places.filterKeys {
            it != drag.index && it < drag.keys.size
        }.values.count { it.center.x < drag.pointer.x }
    }

    /** How far sideways the tab at [index] moves to open the dragged tab's place: by the dragged tab's width. */
    fun shiftOf(
        index: Int,
        drag: TabDrag
    ): Float {
        val slot = slotOf(drag = drag)
        val width = places[drag.index]?.width ?: 0f
        return when {
            index in (drag.index + 1)..slot -> -width
            index in slot..<drag.index -> width
            else -> 0f
        }
    }

    /**
     * Where the dragged tab is drawn: following the pointer, but held on the row's line while the pointer is on it, as if
     * the row held it like a magnet; once let go, where its glide has taken it.
     */
    fun offsetOf(drag: TabDrag): Offset = when {
        drag.landing != null || !row.contains(offset = drag.pointer) -> drag.travel
        else -> Offset(x = drag.travel.x, y = 0f)
    }

    /** How far sideways the dragged tab goes to sit in [slot], from where it was laid out. */
    fun glideTo(
        slot: Int,
        drag: TabDrag
    ): Float {
        val from = places[drag.index] ?: return 0f
        val to = places[slot] ?: return 0f
        return if (slot > drag.index) to.right - from.right else to.left - from.left
    }
}
