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
 * The "Search notes" button (1.6 smoke: search was hidden behind a shortcut), now on the tool rail (1.11): clicking it
 * opens the search, and clicking the tree beside it does not.
 */
class SidebarSearchButtonTest {
    @Test
    fun `clicking search notes in the sidebar opens the search`() {
        var searches = 0
        UiThread.run {
            ImageComposeScene(width = WINDOW_WIDTH, height = WINDOW_HEIGHT, density = Density(density = DENSITY)) {
                NotekeaTheme(darkTheme = false) {
                    VaultTreeContent(
                        state = TreePreviewData.state(),
                        vaultName = TreePreviewData.VAULT_NAME,
                        onIntent = {},
                        rail = SidebarRailActions(onSearch = { searches++ })
                    ) { PreviewNoteContent(path = TreePreviewData.state().selectedPath) }
                }
            }.use { scene ->
                scene.render(nanoTime = 0L)
                listOf(SEARCH_BUTTON, TREE_ROW).forEach { position ->
                    scene.sendPointerEvent(eventType = PointerEventType.Press, position = position)
                    scene.sendPointerEvent(eventType = PointerEventType.Release, position = position)
                    scene.render(nanoTime = FRAME_NANOS)
                }
            }
        }

        assertEquals(
            expected = 1,
            actual = searches
        )
    }

    companion object {
        private const val DENSITY = 1.5f
        private const val FRAME_NANOS = 16_000_000L
        private const val WINDOW_HEIGHT = 560
        private const val WINDOW_WIDTH = 1_100
        private val SEARCH_BUTTON = Offset(x = 30f, y = 183f)
        private val TREE_ROW = Offset(x = 190f, y = 295f)
    }
}
