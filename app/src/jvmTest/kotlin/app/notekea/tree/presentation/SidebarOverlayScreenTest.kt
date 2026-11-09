package app.notekea.tree.presentation

import androidx.compose.foundation.layout.Box
import app.notekea.tree.presentation.view.SidebarRailActions
import app.notekea.tree.presentation.model.TreePreviewData
import app.notekea.tree.presentation.view.VaultTreeContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import app.notekea.tree.domain.TreePreferences
import kotlin.test.Test
import kotlin.test.assertEquals

/** Below 760 dp the sidebar floats over the note instead of taking its room (`ROADMAP.md`, audit 48). */
class SidebarOverlayScreenTest {
    @Test
    fun `a narrow window leaves the note its room, a wide one docks the sidebar`() {
        val narrow = contentWidthAt(width = TreePreferences.SIDEBAR_OVERLAY_BELOW_DP - 1)
        val wide = contentWidthAt(width = TreePreferences.SIDEBAR_OVERLAY_BELOW_DP)

        assertEquals(
            expected = true,
            actual = narrow > wide
        )
    }

    private fun contentWidthAt(
        width: Int
    ): Int {
        var contentWidth = 0
        UiThread.run {
            ImageComposeScene(width = width, height = HEIGHT, density = Density(density = 1f)) {
                NotekeaTheme(darkTheme = false) {
                    VaultTreeContent(
                        state = TreePreviewData.state(),
                        vaultName = TreePreviewData.VAULT_NAME,
                        onIntent = {},
                        rail = SidebarRailActions()
                    ) { Box(modifier = Modifier.fillMaxSize().onSizeChanged { contentWidth = it.width }) }
                }
            }.use { scene -> repeat(times = FRAMES) { scene.render(nanoTime = it * FRAME_NANOS) } }
        }
        return contentWidth
    }

    private companion object {
        const val FRAMES = 3
        const val FRAME_NANOS = 16_000_000L
        const val HEIGHT = 600
    }
}
