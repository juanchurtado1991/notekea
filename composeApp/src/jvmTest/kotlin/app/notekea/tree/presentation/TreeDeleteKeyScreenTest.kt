package app.notekea.tree.presentation

import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import app.notekea.core.platform.domain.OperatingSystem
import app.notekea.core.platform.domain.Shortcut
import app.notekea.core.platform.domain.ShortcutKey
import app.notekea.core.platform.domain.ShortcutModifier
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.preview.PreviewPlatformConventions
import app.notekea.core.presentation.theme.NotekeaTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Smoke 1.4-13 on macOS: the Delete key of a Mac keyboard (Backspace) did nothing in the list of notes, where only
 * Cmd+Backspace deleted. Both ask to delete the selected note now.
 */
@OptIn(InternalComposeUiApi::class)
class TreeDeleteKeyScreenTest {
    private var frame = 0L

    @Test
    fun `on macOS backspace alone asks to delete the selected note, as cmd+backspace does`() {
        val intents = mutableListOf<VaultTreeIntent>()
        val macos = PreviewPlatformConventions.linux.copy(
            operatingSystem = OperatingSystem.MACOS,
            deleteShortcut = Shortcut(key = ShortcutKey.BACKSPACE, modifiers = setOf(ShortcutModifier.COMMAND)),
            alternateDeleteShortcut = Shortcut(key = ShortcutKey.BACKSPACE, modifiers = emptySet())
        )
        UiThread.run {
            ImageComposeScene(width = WIDTH, height = HEIGHT, density = Density(density = 1f)) {
                NotekeaTheme(darkTheme = false) {
                    TreeList(state = TreePreviewData.state().copy(platformConventions = macos), onIntent = {
                        intents +=
                            it
                    })
                }
            }.use { scene ->
                settle(scene = scene)
                scene.sendKeyEvent(event = KeyEvent(key = Key.Backspace, type = KeyEventType.KeyDown))
                settle(scene = scene)
            }
        }

        assertEquals(expected = listOf<VaultTreeIntent>(VaultTreeIntent.OnDeleteRequest()), actual = intents)
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
        private const val FRAME_NANOS = 16_000_000L
        private const val HEIGHT = 480
        private const val SETTLE_FRAMES = 5
        private const val WIDTH = 320
    }
}
