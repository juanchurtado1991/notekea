package app.notekea.core.presentation.platform

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The window grows for a vault's workspace and shrinks back for the welcome screen (`ROADMAP.md` 1.11). */
class WindowSizesTest {
    @Test
    fun `the workspace window is larger than welcome, a larger or maximized window keeps its size`() {
        val window = WindowState(size = WindowSizes.welcome)
        WindowSizes.fit(state = window, isWorkspace = true)
        val workspace = window.size
        WindowSizes.fit(state = window, isWorkspace = false)
        val backToWelcome = window.size
        val large = WindowState(size = DpSize(width = 5_000.dp, height = 3_000.dp))
        WindowSizes.fit(state = large, isWorkspace = true)
        val maximized =
            WindowState(placement = WindowPlacement.Maximized, size = DpSize(width = 800.dp, height = 600.dp))
        WindowSizes.fit(state = maximized, isWorkspace = true)

        assertTrue(
            actual = workspace.width > WindowSizes.welcome.width && workspace.height > WindowSizes.welcome.height
        )
        assertEquals(
            expected = listOf(
                WindowSizes.welcome,
                DpSize(width = 5_000.dp, height = 3_000.dp),
                DpSize(width = 800.dp, height = 600.dp)
            ),
            actual = listOf(backToWelcome, large.size, maximized.size)
        )
    }
}
