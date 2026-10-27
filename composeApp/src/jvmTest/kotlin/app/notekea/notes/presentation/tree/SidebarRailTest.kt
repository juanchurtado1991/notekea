package app.notekea.notes.presentation.tree

import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.theme.NotekeaTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The tool rail and the sidebar header (1.11 smoke request, as in Android Studio): the rail's Files, side panel, Search,
 * Today, Open another vault, About and Settings work with the sidebar shown or hidden, and the header expands,
 * collapses and minimizes.
 */
class SidebarRailTest {
    @Test
    fun `the rail's buttons work with the sidebar shown or hidden, and the header expands, collapses and minimizes`() {
        val shown = clicks(isCollapsed = false, positions = RAIL + HEADER)
        val hidden = clicks(isCollapsed = true, positions = RAIL)

        val railIntents = listOf<Any>(
            VaultTreeIntent.OnSidebarToggle,
            SIDE_PANEL,
            SEARCH,
            VaultTreeIntent.OnTodayClick,
            SWITCH_VAULT,
            ABOUT,
            SETTINGS
        )
        assertEquals(
            expected = (
                railIntents + listOf(
                    VaultTreeIntent.OnExpandAll,
                    VaultTreeIntent.OnCollapseAll,
                    VaultTreeIntent.OnSidebarToggle
                )
                ) to railIntents,
            actual = shown to hidden
        )
    }

    private fun clicks(
        isCollapsed: Boolean,
        positions: List<Offset>
    ): List<Any> {
        val intents = mutableListOf<Any>()
        UiThread.run {
            ImageComposeScene(width = WIDTH, height = HEIGHT, density = Density(density = 1f)) {
                NotekeaTheme(darkTheme = false) {
                    VaultTreeContent(
                        state = TreePreviewData.state().copy(isSidebarCollapsed = isCollapsed),
                        vaultName = TreePreviewData.VAULT_NAME,
                        onIntent = { intents += it },
                        rail = SidebarRailActions(
                            onSearch = { intents += SEARCH },
                            onSidePanelToggle = { intents += SIDE_PANEL },
                            onSwitchVault = { intents += SWITCH_VAULT },
                            onAbout = { intents += ABOUT },
                            onSettings = { intents += SETTINGS }
                        )
                    ) {}
                }
            }.use { scene ->
                scene.render(nanoTime = 0L)
                positions.forEachIndexed { index, position ->
                    scene.sendPointerEvent(eventType = PointerEventType.Press, position = position)
                    scene.sendPointerEvent(eventType = PointerEventType.Release, position = position)
                    scene.render(nanoTime = (index + 1) * FRAME_NANOS)
                }
            }
        }
        return intents
    }

    companion object {
        private const val FRAME_NANOS = 16_000_000L
        private const val HEIGHT = 420
        private const val WIDTH = 1000
        private val HEADER = listOf(Offset(x = 307f, y = 26f), Offset(x = 335f, y = 26f), Offset(x = 363f, y = 26f))
        private const val ABOUT = "about"
        private const val SEARCH = "search"
        private const val SETTINGS = "settings"
        private const val SIDE_PANEL = "side panel"
        private const val SWITCH_VAULT = "switch vault"
        private val RAIL = listOf(21f, 53f, 86f, 118f, 334f, 366f, 398f).map { Offset(x = 20f, y = it) }
    }
}
