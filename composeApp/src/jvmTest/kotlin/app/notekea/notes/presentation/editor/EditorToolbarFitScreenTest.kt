package app.notekea.notes.presentation.editor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.use
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.clickDescription
import app.notekea.core.presentation.clickText
import app.notekea.core.presentation.descriptions
import app.notekea.core.presentation.preview.PreviewPlatformConventions
import app.notekea.core.presentation.theme.NotekeaTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The editor toolbar in a narrow editor (`ROADMAP.md` 1.14), rendered headless: it stays one line, and the buttons that
 * do not fit open from More, where they still work (1.14 smoke: the buttons took a line of their own).
 */
class EditorToolbarFitScreenTest {
    private var frame = 0L

    @Test
    fun `a narrow toolbar moves the links, lists and template into More, where they still work`() {
        val intents = mutableListOf<EditorIntent>()
        var buttons = emptyList<String>()
        UiThread.run {
            ImageComposeScene(width = WIDTH, height = HEIGHT, density = Density(density = 1f)) {
                NotekeaTheme(darkTheme = false) {
                    Box(modifier = Modifier.width(width = TOOLBAR_WIDTH_DP.dp)) {
                        EditorToolbar(
                            isReading = false,
                            platformConventions = PreviewPlatformConventions.linux,
                            onIntent = { intents += it }
                        )
                    }
                }
            }.use { scene ->
                settle(scene = scene)
                buttons = scene.descriptions()
                scene.clickDescription(description = MORE) { settle(scene = scene) }
                scene.clickText(text = TEMPLATE) { settle(scene = scene) }
            }
        }

        assertEquals(
            expected = Triple(false, false, listOf<EditorIntent>(EditorIntent.OnTemplateRequest)),
            actual = Triple(LINK in buttons, TEMPLATE in buttons, intents)
        )
    }

    private fun settle(
        scene: ImageComposeScene
    ) {
        repeat(times = SETTLE_FRAMES) {
            frame += FRAME_NANOS
            scene.render(nanoTime = frame)
        }
    }

    companion object {
        private const val FRAME_NANOS = 160_000_000L
        private const val HEIGHT = 600
        private const val LINK = "Link to a note"
        private const val MORE = "More"
        private const val SETTLE_FRAMES = 4
        private const val TEMPLATE = "Insert template"
        private const val TOOLBAR_WIDTH_DP = 400
        private const val WIDTH = 700
    }
}
