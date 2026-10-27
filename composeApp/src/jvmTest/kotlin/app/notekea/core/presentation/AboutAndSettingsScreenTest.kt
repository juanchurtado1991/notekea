package app.notekea.core.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import app.notekea.core.presentation.about.AboutDialog
import app.notekea.core.presentation.preview.PreviewPlatformConventions
import app.notekea.core.presentation.settings.SettingsDialog
import app.notekea.core.presentation.theme.NotekeaTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/** About and Settings over the window, rendered headless: each closes with its ✕. */
class AboutAndSettingsScreenTest {
    private var frame = 0L

    @Test
    fun `About and Settings close with their close buttons`() {
        var dismissed = 0
        UiThread.run {
            listOf<Pair<Offset, @Composable () -> Unit>>(
                ABOUT_CLOSE to { AboutDialog(platformConventions = PreviewPlatformConventions.linux) { dismissed++ } },
                SETTINGS_CLOSE to { SettingsDialog { dismissed++ } }
            ).forEach { (close, dialog) ->
                ImageComposeScene(width = WIDTH, height = HEIGHT, density = Density(density = 1f)) {
                    NotekeaTheme(darkTheme = false) { dialog() }
                }.use { scene ->
                    settle(scene = scene)
                    scene.sendPointerEvent(eventType = PointerEventType.Move, position = close)
                    scene.sendPointerEvent(eventType = PointerEventType.Press, position = close)
                    scene.sendPointerEvent(eventType = PointerEventType.Release, position = close)
                    settle(scene = scene)
                }
            }
        }

        assertEquals(
            expected = 2,
            actual = dismissed
        )
    }

    private fun settle(
        scene: ImageComposeScene
    ) {
        repeat(times = SETTLE_FRAMES) { scene.render(nanoTime = (frame++) * FRAME_NANOS) }
    }

    companion object {
        private const val FRAME_NANOS = 16_000_000L
        private const val HEIGHT = 640
        private const val SETTLE_FRAMES = 4
        private const val WIDTH = 1000
        private val ABOUT_CLOSE = Offset(x = 852f, y = 98f)
        private val SETTINGS_CLOSE = Offset(x = 857f, y = 92f)
    }
}
