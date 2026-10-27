package app.notekea.core.presentation.platform

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import kotlinx.coroutines.test.runTest
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
    fun `the workspace window is larger than welcome, a larger or maximized window keeps its size`() = runTest {
        val window = FakeWindow(sizes = sizes, size = sizes.welcome)
        window.fit(isWorkspace = true)
        val workspace = window.shown
        window.fit(isWorkspace = false)
        val backToWelcome = window.shown
        val large = FakeWindow(sizes = sizes, size = DpSize(width = 5_000.dp, height = 3_000.dp))
        large.fit(isWorkspace = true)
        val maximized = FakeWindow(
            sizes = sizes,
            size = DpSize(width = 800.dp, height = 600.dp),
            placement = WindowPlacement.Maximized
        )
        maximized.fit(isWorkspace = true)

        assertEquals(
            expected = listOf(
                DpSize(width = 1440.dp, height = 900.dp),
                DpSize(width = 960.dp, height = 680.dp),
                DpSize(width = 5_000.dp, height = 3_000.dp),
                DpSize(width = 800.dp, height = 600.dp)
            ),
            actual = listOf(workspace, backToWelcome, large.shown, maximized.shown)
        )
    }

    @Test
    fun `a window put back to its old size by the window manager while it opens gets the workspace size again`() =
        runTest {
            // 1.11 smoke: opening a vault, at launch or from the welcome screen, sometimes left the welcome size.
            val window = FakeWindow(sizes = sizes, size = sizes.welcome, revertsFirstResize = true)

            window.fit(isWorkspace = true)

            assertEquals(
                expected = DpSize(width = 1440.dp, height = 900.dp) to 2,
                actual = window.shown to window.resizes
            )
        }

    @Test
    fun `on a small screen both windows fit its usable area, and without a screen they keep their sizes`() {
        val small = WindowSizes(usableArea = { SMALL_SCREEN })
        val headless = WindowSizes(usableArea = { null })

        assertEquals(
            expected = listOf(
                DpSize(width = 960.dp, height = 600.dp),
                SMALL_SCREEN,
                DpSize(width = 960.dp, height = 680.dp)
            ),
            actual = listOf(
                small.welcome,
                small.targetOf(state = WindowState(size = small.welcome), isWorkspace = true),
                headless.welcome
            )
        )
    }

    /**
     * A window on screen: [shown] is its size there, changed by a resize unless the window manager puts the first one
     * back ([revertsFirstResize]), as it may while the window is being shown.
     */
    private class FakeWindow(
        private val sizes: WindowSizes,
        size: DpSize,
        placement: WindowPlacement = WindowPlacement.Floating,
        private val revertsFirstResize: Boolean = false
    ) {
        val state = WindowState(placement = placement, size = size)
        var shown = size
        var resizes = 0

        suspend fun fit(isWorkspace: Boolean) {
            sizes.fit(
                state = state,
                isWorkspace = isWorkspace,
                shownSize = { shown },
                resize = { size ->
                    resizes++
                    if (revertsFirstResize && resizes == 1) {
                        state.size = shown
                    } else {
                        state.size = size
                        shown = size
                    }
                },
                settle = {}
            )
        }
    }

    companion object {
        private val LARGE_SCREEN = DpSize(width = 2560.dp, height = 1400.dp)
        private val SMALL_SCREEN = DpSize(width = 1024.dp, height = 600.dp)
    }
}
