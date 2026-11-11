package app.notekea.core.presentation.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/**
 * The system's title bar when the window's content runs under it, as on macOS with the unified title bar (redesign
 * 1A): its [height], the [leading] width at the content's start the window buttons take (none once a sidebar is
 * there), and a [dragArea] that moves the window where nothing else is. The buttons stay the system's own.
 */
@Immutable
class TitleBarSpace(
    val height: Dp,
    val leading: Dp,
    val dragArea: @Composable (Modifier) -> Unit
) {
    /** The same title bar for content that starts [leading] after the window's edge. */
    fun withLeading(leading: Dp): TitleBarSpace = TitleBarSpace(
        height = height,
        leading = leading,
        dragArea = dragArea
    )
}
