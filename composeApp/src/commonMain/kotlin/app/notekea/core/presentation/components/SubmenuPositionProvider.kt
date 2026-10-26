package app.notekea.core.presentation.components

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider

/**
 * Places a submenu beside its row, as desktop menus do: to the right, or to the left when the window has no room
 * there, its top level with the row's and moved up as far as needed to stay inside the window.
 */
class SubmenuPositionProvider : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        val fitsRight = anchorBounds.right + popupContentSize.width <= windowSize.width
        val x = if (fitsRight) anchorBounds.right else anchorBounds.left - popupContentSize.width
        val y = anchorBounds.top.coerceAtMost(maximumValue = windowSize.height - popupContentSize.height)
        return IntOffset(x = x.coerceAtLeast(minimumValue = 0), y = y.coerceAtLeast(minimumValue = 0))
    }
}
