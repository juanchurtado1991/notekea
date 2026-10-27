package app.notekea.core.presentation.platform

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import java.awt.GraphicsEnvironment
import java.awt.HeadlessException
import kotlinx.coroutines.delay

/**
 * The main window's size for each screen (`ROADMAP.md` 1.11): the welcome screen (choosing a vault) is small, and a
 * vault's workspace is large enough to show, as it opens, the tool rail, the sidebar, a note at a readable width and
 * the outline beside it. Never larger than the screen's usable area (without the taskbar or dock); the window is centred
 * when its size changes, and a window the user maximized, or made larger, keeps its size. The usable area comes from
 * [usableArea] (none when there is no screen to ask), [system] asking the computer's.
 */
class WindowSizes(
    private val usableArea: () -> DpSize?
) {
    val welcome: DpSize
        get() = fitted(size = WELCOME)

    /**
     * The size [state] should have for the workspace ([isWorkspace]) or the welcome screen: none while it is maximized
     * or full screen.
     */
    fun targetOf(
        state: WindowState,
        isWorkspace: Boolean
    ): DpSize? = when {
        state.placement != WindowPlacement.Floating -> null

        isWorkspace -> fitted(size = WORKSPACE).let {
            DpSize(width = maxOf(it.width, state.size.width), height = maxOf(it.height, state.size.height))
        }

        else -> welcome
    }

    /**
     * Gives the window of [state] its size for the workspace ([isWorkspace]) or the welcome screen, and keeps checking
     * for a while ([settle] between checks), because the window manager may answer a resize asked for while the
     * window is being shown with the size it had before: whenever the size on screen ([shownSize]) is not the one it
     * should have, it is asked again ([resize]).
     */
    suspend fun fit(
        state: WindowState,
        isWorkspace: Boolean,
        shownSize: () -> DpSize,
        resize: (DpSize) -> Unit,
        settle: suspend () -> Unit = { delay(timeMillis = SETTLE_MILLIS) }
    ) {
        repeat(times = SETTLE_CHECKS) {
            val target = targetOf(state = state, isWorkspace = isWorkspace) ?: return
            if (shownSize() != target) resize(target)
            settle()
        }
    }

    /** [size] made to fit the screen's usable area; as it is when there is no screen to ask. */
    private fun fitted(
        size: DpSize
    ): DpSize {
        val area = usableArea() ?: return size
        return DpSize(width = minOf(size.width, area.width), height = minOf(size.height, area.height))
    }

    companion object {
        private val WELCOME = DpSize(width = 960.dp, height = 680.dp)
        private val WORKSPACE = DpSize(width = 1440.dp, height = 900.dp)
        private const val SETTLE_CHECKS = 5
        private const val SETTLE_MILLIS = 200L

        /** The sizes for this computer's main screen. */
        val system = WindowSizes(usableArea = ::screenArea)

        /** The main screen's area without the taskbar or dock; none without a screen. */
        private fun screenArea(): DpSize? = try {
            val bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().maximumWindowBounds
            DpSize(width = bounds.width.dp, height = bounds.height.dp)
        } catch (_: HeadlessException) {
            null
        }
    }
}
