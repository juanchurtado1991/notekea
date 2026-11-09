package app.notekea.core.presentation.platform

import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import app.notekea.core.appearance.domain.WindowBounds
import app.notekea.tree.domain.TreePreferences
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The window comes back as it was left, on a screen that shows it (audit 48). */
class WindowBoundsTest {
    private val sizes = WindowSizes(usableArea = { SCREEN_AREA }, screens = { listOf(SCREEN) })

    @Test
    fun `the window comes back at its size and position, maximized if it was`() {
        assertEquals(
            expected = listOf(
                WindowOpening(
                    placement = WindowPlacement.Floating,
                    size = DpSize(width = 900.dp, height = 700.dp),
                    position = WindowPosition.Absolute(x = 40.dp, y = 60.dp)
                ),
                WindowOpening(
                    placement = WindowPlacement.Maximized,
                    size = DpSize(width = 900.dp, height = 700.dp),
                    position = WindowPosition.Absolute(x = 40.dp, y = 60.dp)
                )
            ),
            actual = listOf(sizes.opening(kept = KEPT), sizes.opening(kept = KEPT.copy(isMaximized = true)))
        )
    }

    @Test
    fun `a screen that is gone centers the window, and a size beyond the limits is brought back`() {
        val opening = sizes.opening(kept = KEPT.copy(widthDp = 100, heightDp = 5_000, xDp = 4_000, yDp = 60))

        assertEquals(
            expected = WindowOpening(
                placement = WindowPlacement.Floating,
                size = DpSize(width = WindowSizes.MINIMUM.width, height = SCREEN_AREA.height),
                position = sizes.opening(kept = null).position
            ),
            actual = opening
        )
    }

    @Test
    fun `the minimum leaves a note its room and reaches the sidebar's overlay`() {
        assertTrue(actual = WindowSizes.MINIMUM.width >= (NOTE_MIN_WIDTH_DP + RAIL_WIDTH_DP).dp)
        assertTrue(actual = WindowSizes.MINIMUM.width < TreePreferences.SIDEBAR_OVERLAY_BELOW_DP.dp)
    }

    @Test
    fun `the window's state becomes what happened to it`() {
        val position = WindowPosition.Absolute(x = 40.dp, y = 60.dp)
        val size = DpSize(width = 900.dp, height = 700.dp)

        assertEquals(
            expected = listOf(WindowBoundsIntent.OnFloating(bounds = KEPT), WindowBoundsIntent.OnMaximized, null),
            actual = listOf(WindowPlacement.Floating, WindowPlacement.Maximized, WindowPlacement.Fullscreen).map {
                windowBoundsIntent(placement = it, size = size, position = position)
            }
        )
    }

    @Test
    fun `the title says the active tab, the vault and the app`() {
        assertEquals(
            expected = listOf("Arepas — Playground — Notekea", "Playground — Notekea", "Notekea"),
            actual = listOf(
                WindowTitle.of(vaultName = "Playground", activeTabName = "Arepas"),
                WindowTitle.of(vaultName = "Playground", activeTabName = null),
                WindowTitle.of(vaultName = null, activeTabName = null)
            )
        )
    }

    private companion object {
        const val NOTE_MIN_WIDTH_DP = 480
        const val RAIL_WIDTH_DP = 40
        val KEPT = WindowBounds(widthDp = 900, heightDp = 700, xDp = 40, yDp = 60, isMaximized = false)
        val SCREEN = DpRect(left = 0.dp, top = 0.dp, right = 1920.dp, bottom = 1080.dp)
        val SCREEN_AREA = DpSize(width = 1920.dp, height = 1040.dp)
    }
}
