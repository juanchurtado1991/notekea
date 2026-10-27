package app.notekea.core.presentation.platform

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The window grows for a vault's workspace and shrinks back for the welcome screen (`ROADMAP.md` 1.11), never beyond
 * the screen's usable area. The screen is given, so the result does not depend on the computer running the test (CI:
 * the macOS runner's screen is smaller than the workspace).
 */
class WindowSizesTest {
    private val sizes = WindowSizes(usableArea = { LARGE_SCREEN })

    @Test
    fun `the workspace window is larger than welcome, a larger or maximized window keeps its size`() {
        val window = WindowState(size = sizes.welcome)
        sizes.fit(state = window, isWorkspace = true)
        val workspace = window.size
        sizes.fit(state = window, isWorkspace = false)
        val backToWelcome = window.size
        val large = WindowState(size = DpSize(width = 5_000.dp, height = 3_000.dp))
        sizes.fit(state = large, isWorkspace = true)
        val maximized =
            WindowState(placement = WindowPlacement.Maximized, size = DpSize(width = 800.dp, height = 600.dp))
        sizes.fit(state = maximized, isWorkspace = true)

        assertEquals(
            expected = listOf(
                DpSize(width = 1440.dp, height = 900.dp),
                DpSize(width = 960.dp, height = 680.dp),
                DpSize(width = 5_000.dp, height = 3_000.dp),
                DpSize(width = 800.dp, height = 600.dp)
            ),
            actual = listOf(workspace, backToWelcome, large.size, maximized.size)
        )
    }

    @Test
    fun `on a small screen both windows fit its usable area, and without a screen they keep their sizes`() {
        val small = WindowSizes(usableArea = { SMALL_SCREEN })
        val window = WindowState(size = small.welcome)
        val welcome = window.size
        small.fit(state = window, isWorkspace = true)
        val headless = WindowSizes(usableArea = { null })

        assertEquals(
            expected = listOf(
                DpSize(width = 960.dp, height = 600.dp),
                SMALL_SCREEN,
                DpSize(width = 960.dp, height = 680.dp)
            ),
            actual = listOf(welcome, window.size, headless.welcome)
        )
    }

    companion object {
        private val LARGE_SCREEN = DpSize(width = 2560.dp, height = 1400.dp)
        private val SMALL_SCREEN = DpSize(width = 1024.dp, height = 600.dp)
    }
}
