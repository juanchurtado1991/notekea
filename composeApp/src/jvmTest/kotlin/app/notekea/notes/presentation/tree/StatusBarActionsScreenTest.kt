package app.notekea.notes.presentation.tree

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.clickText
import app.notekea.core.presentation.platform.LocalUiZoom
import app.notekea.core.presentation.platform.UiZoomState
import app.notekea.core.presentation.texts
import app.notekea.core.presentation.theme.NotekeaTheme
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.presentation.workspace.TabKind
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The status bar's clickable facts (`ROADMAP.md` 1.14), rendered headless: a folder or the file of the path shows it in
 * Files, and the zoom, shown only while it is not 100 %, goes back to 100 % on a click.
 */
class StatusBarActionsScreenTest {
    @Test
    fun `a folder and the file of the path are shown in Files, and the zoom shows only while not 100 percent`() {
        val revealed = mutableListOf<RelativePath>()
        var resets = 0
        var zoomedTexts = emptyList<String>()
        val normalTexts = texts(zoom = UiZoomState())
        UiThread.run {
            val zoom = UiZoomState(scale = ZOOMED, onReset = { resets++ })
            scene(zoom = zoom, onReveal = { revealed += it }).use { scene ->
                scene.render(nanoTime = 0L)
                zoomedTexts = scene.texts()
                listOf("Editor", "Tasks and callouts", "120 %").forEach { text ->
                    scene.clickText(text = text) { scene.render(nanoTime = 0L) }
                }
            }
        }

        assertEquals(
            expected = listOf(
                false,
                true,
                listOf(RelativePath(value = "Editor"), RelativePath(value = NOTE)),
                1
            ),
            actual = listOf("120 %" in normalTexts, "120 %" in zoomedTexts, revealed, resets)
        )
    }

    private fun texts(
        zoom: UiZoomState
    ): List<String> {
        var texts = emptyList<String>()
        UiThread.run {
            scene(zoom = zoom, onReveal = {}).use { scene ->
                scene.render(nanoTime = 0L)
                texts = scene.texts()
            }
        }
        return texts
    }

    private fun scene(
        zoom: UiZoomState,
        onReveal: (RelativePath) -> Unit
    ) = ImageComposeScene(width = WIDTH, height = HEIGHT, density = Density(density = 1f)) {
        CompositionLocalProvider(LocalUiZoom provides zoom) {
            NotekeaTheme(darkTheme = false) {
                WorkspaceStatusBar(
                    vaultName = VAULT,
                    isLoading = false,
                    onReveal = onReveal,
                    path = RelativePath(value = NOTE),
                    kind = TabKind.NOTE
                )
            }
        }
    }

    companion object {
        private const val HEIGHT = 40
        private const val ZOOMED = 1.2f
        private const val NOTE = "Editor/Tasks and callouts.md"
        private const val VAULT = "Playground Vault"
        private const val WIDTH = 1300
    }
}
