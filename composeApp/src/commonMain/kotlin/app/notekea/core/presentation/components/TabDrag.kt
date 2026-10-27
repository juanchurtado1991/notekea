package app.notekea.core.presentation.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset

/**
 * A [NotekeaTabStrip] tab being dragged: the one at [index] when the drag began, among the tabs in [keys]' order,
 * pressed at [start] in the window and moved by [travel] since. Once let go on the row, it glides into [landing], its
 * place among the others, and waits there for the strip's tabs to take their new order.
 */
internal class TabDrag(
    val index: Int,
    val keys: List<Any>,
    val start: Offset
) {
    var travel by mutableStateOf(value = Offset.Zero)
    var landing by mutableStateOf<Int?>(value = null)

    /** Where the pointer is in the window. */
    val pointer: Offset get() = start + travel
}
