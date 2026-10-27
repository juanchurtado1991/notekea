package app.notekea.core.presentation.platform

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The window opens at one size for the welcome screen and the workspace (`ROADMAP.md` 1.11), never beyond the screen's
 * usable area. The screen is given, so the result does not depend on the computer running the test (CI: the macOS
 * runner's screen is smaller than the window).
 */
class WindowSizesTest {
    @Test
    fun `the window opens at the workspace size, cut to a small screen, and as it is without a screen`() {
        assertEquals(
            expected = listOf(
                DpSize(width = 1440.dp, height = 900.dp),
                DpSize(width = 1024.dp, height = 600.dp),
                DpSize(width = 1440.dp, height = 900.dp)
            ),
            actual = listOf(LARGE_SCREEN, SMALL_SCREEN, null).map { WindowSizes(usableArea = { it }).initial }
        )
    }

    companion object {
        private val LARGE_SCREEN = DpSize(width = 2560.dp, height = 1400.dp)
        private val SMALL_SCREEN = DpSize(width = 1024.dp, height = 600.dp)
    }
}
