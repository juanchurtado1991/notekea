package app.notekea.core.presentation.components

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider

/**
 * Places a dropdown under its anchor, its start edge on the anchor's, as desktop pickers do: above the anchor when the
 * window has no room below, and moved left as far as needed to stay inside the window.
 */
class DropdownPositionProvider(
    private val gap: Int = 0
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        val below = anchorBounds.bottom + gap
        val fitsBelow = below + popupContentSize.height <= windowSize.height
        val y = if (fitsBelow) below else anchorBounds.top - gap - popupContentSize.height
        val x = anchorBounds.left.coerceAtMost(maximumValue = windowSize.width - popupContentSize.width)
        return IntOffset(x = x.coerceAtLeast(minimumValue = 0), y = y.coerceAtLeast(minimumValue = 0))
    }
}
