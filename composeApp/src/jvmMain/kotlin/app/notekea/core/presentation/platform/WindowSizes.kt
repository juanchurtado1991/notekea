package app.notekea.core.presentation.platform

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import java.awt.GraphicsEnvironment
import java.awt.HeadlessException

/**
 * The main window's size for each screen (`ROADMAP.md` 1.11): the welcome screen (choosing a vault) is small, and a
 * vault's workspace is large enough to show, as it opens, the tool rail, the sidebar, a note at a readable width and
 * the outline beside it. Never larger than the screen's usable area (without the taskbar or dock); the window is centred
 * when its screen changes, and a window the user maximized, or made larger, keeps its size.
 */
object WindowSizes {
    private val WELCOME = DpSize(width = 960.dp, height = 680.dp)
    private val WORKSPACE = DpSize(width = 1440.dp, height = 900.dp)

    val welcome: DpSize
        get() = fitted(size = WELCOME)

    /** Resizes [state] for the workspace ([isWorkspace]) or the welcome screen, after the other one was shown. */
    fun fit(
        state: WindowState,
        isWorkspace: Boolean
    ) {
        if (state.placement != WindowPlacement.Floating) return
        val target = if (isWorkspace) {
            fitted(size = WORKSPACE).let {
                DpSize(maxOf(it.width, state.size.width), maxOf(it.height, state.size.height))
            }
        } else {
            welcome
        }
        if (target == state.size) return
        state.size = target
        state.position = WindowPosition.Aligned(alignment = Alignment.Center)
    }

    /** [size] made to fit the screen's usable area; as it is when there is no screen to ask. */
    private fun fitted(
        size: DpSize
    ): DpSize = try {
        val bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().maximumWindowBounds
        DpSize(width = minOf(size.width, bounds.width.dp), height = minOf(size.height, bounds.height.dp))
    } catch (_: HeadlessException) {
        size
    }
}
