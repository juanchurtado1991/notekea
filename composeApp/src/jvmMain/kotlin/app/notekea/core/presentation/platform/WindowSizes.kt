package app.notekea.core.presentation.platform

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import java.awt.GraphicsEnvironment
import java.awt.HeadlessException

/**
 * The main window's size (`ROADMAP.md` 1.11), the same for the welcome screen and a vault's workspace, so the window
 * never changes size by itself: large enough to show, as a vault opens, the tool rail, the sidebar, a note at a
 * readable width and the outline beside it, but never larger than the screen's usable area (without the taskbar or
 * dock), which comes from [usableArea] (none when there is no screen to ask), [system] asking the computer's.
 */
class WindowSizes(
    private val usableArea: () -> DpSize?
) {
    /** The window's size as it opens. */
    val initial: DpSize
        get() {
            val area = usableArea() ?: return WINDOW
            return DpSize(width = minOf(WINDOW.width, area.width), height = minOf(WINDOW.height, area.height))
        }

    companion object {
        private val WINDOW = DpSize(width = 1440.dp, height = 900.dp)

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
