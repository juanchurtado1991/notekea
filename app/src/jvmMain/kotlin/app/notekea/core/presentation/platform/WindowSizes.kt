package app.notekea.core.presentation.platform

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import app.notekea.core.appearance.domain.WindowBounds
import java.awt.GraphicsEnvironment
import java.awt.HeadlessException

/**
 * The main window's size, the same for the welcome screen and a vault's workspace: large enough for the tool rail, the
 * sidebar, a note at a readable width and the outline, but never larger than the screen's usable area ([usableArea]).
 * [screens] are those connected now: a window comes back only where one can show it.
 */
class WindowSizes(
    private val usableArea: () -> DpSize?,
    private val screens: () -> List<DpRect> = { emptyList() }
) {
    /** The window's size as it opens the first time. */
    val initial: DpSize
        get() {
            val area = usableArea() ?: return WINDOW

            return DpSize(
                width = minOf(WINDOW.width, area.width),
                height = minOf(WINDOW.height, area.height)
            )
        }

    /**
     * How the window opens: as it was [kept], on a screen that shows it and within its limits, or as the first time.
     */
    fun opening(kept: WindowBounds?): WindowOpening {
        val isMaximized = kept?.isMaximized == true
        val centered = WindowPosition.Aligned(alignment = Alignment.Center)

        return WindowOpening(
            placement = if (isMaximized) WindowPlacement.Maximized else WindowPlacement.Floating,
            size = kept?.let { sizeOf(kept = it) } ?: initial,
            position = kept?.let { positionOf(kept = it) } ?: centered
        )
    }

    private fun sizeOf(kept: WindowBounds): DpSize {
        val area = usableArea() ?: WINDOW

        return DpSize(
            width = kept
                .widthDp
                .dp
                .coerceIn(MINIMUM.width, maxOf(MINIMUM.width, area.width)),
            height = kept
                .heightDp
                .dp
                .coerceIn(MINIMUM.height, maxOf(MINIMUM.height, area.height))
        )
    }

    /** Where it was kept, if a screen still shows its title bar: a screen may be gone since the last run. */
    private fun positionOf(kept: WindowBounds): WindowPosition? {
        val x = kept.xDp?.dp ?: return null
        val y = kept.yDp?.dp ?: return null

        return WindowPosition.Absolute(
            x = x,
            y = y
        ).takeIf {
            screens().any {
                it.shows(
                    x = x,
                    y = y
                )
            }
        }
    }

    private fun DpRect.shows(
        x: Dp,
        y: Dp
    ): Boolean = x + TITLE_BAR_GRIP in left..right && y + TITLE_BAR_GRIP in top..bottom

    companion object {
        /** The tool rail and a note at its narrowest (480 dp); below 760 dp the sidebar floats over the note. */
        val MINIMUM = DpSize(
            width = 560.dp,
            height = 400.dp
        )
        private val TITLE_BAR_GRIP = 16.dp
        private val WINDOW = DpSize(
            width = 1440.dp,
            height = 900.dp
        )

        /** The sizes for this computer's main screen. */
        val system = WindowSizes(
            usableArea = ::screenArea,
            screens = ::connectedScreens
        )

        /** The main screen's area without the taskbar or dock; none without a screen. */
        private fun screenArea(): DpSize? = try {
            val bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().maximumWindowBounds

            DpSize(
                width = bounds.width.dp,
                height = bounds.height.dp
            )
        } catch (_: HeadlessException) {
            null
        }

        private fun connectedScreens(): List<DpRect> = try {
            GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .screenDevices
                .map { screen ->
                    val bounds = screen.defaultConfiguration.bounds

                    DpRect(
                        left = bounds.x.dp,
                        top = bounds.y.dp,
                        right = (bounds.x + bounds.width).dp,
                        bottom = (bounds.y + bounds.height).dp
                    )
                }
        } catch (_: HeadlessException) {
            emptyList()
        }
    }
}
