package app.notekea.core.presentation.platform

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState

/**
 * macOS's unified title bar (redesign 1A): the window's content runs under its title bar, which turns transparent and
 * hides the title, so the sidebar reaches the top and the tabs share the row with the traffic lights. The traffic
 * lights stay the system's own; Windows and Linux keep their title bar.
 */
object MacTitleBar {
    private const val FULL_CONTENT = "apple.awt.fullWindowContent"
    private const val TRANSPARENT = "apple.awt.transparentTitleBar"
    private const val TITLE_VISIBLE = "apple.awt.windowTitleVisible"
    private val HEIGHT = 28.dp
    private val LIGHTS_WIDTH = 76.dp

    /** The title bar of this [FrameWindowScope]'s window, unified once; a double click on it zooms, as on macOS. */
    @Composable
    fun FrameWindowScope.rememberTitleBar(state: WindowState): TitleBarSpace {
        val scope = this

        return remember(scope) {
            window.rootPane.putClientProperty(FULL_CONTENT, true)
            window.rootPane.putClientProperty(TRANSPARENT, true)
            window.rootPane.putClientProperty(TITLE_VISIBLE, false)

            TitleBarSpace(
                height = HEIGHT,
                leading = LIGHTS_WIDTH,
                dragArea = { modifier ->
                    scope
                        .WindowDraggableArea(modifier = modifier) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(state) {
                                        detectTapGestures(onDoubleTap = { state.toggleZoom() })
                                    }
                            )
                        }
                }
            )
        }
    }

    private fun WindowState.toggleZoom() {
        placement = if (placement == WindowPlacement.Maximized) {
            WindowPlacement.Floating
        } else {
            WindowPlacement.Maximized
        }
    }
}
