package app.notekea.core.presentation.components

import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.theme.NotekeaTheme
import javax.swing.JPanel
import kotlin.test.Test
import kotlin.test.assertEquals
import java.awt.event.KeyEvent as AwtKeyEvent

/**
 * The palette's search field opened with text already typed (a `#tag` to search, the selected text to tag) puts the
 * cursor after it, so typing continues the text (1.17 smoke: the cursor started before it).
 */
class NotekeaPaletteTest {
    @OptIn(InternalComposeUiApi::class)
    @Test
    fun `text given when the palette opens is continued by typing`() {
        val queries = mutableListOf<String>()
        UiThread.run {
            ImageComposeScene(width = WINDOW_WIDTH, height = WINDOW_HEIGHT, density = Density(density = DENSITY)) {
                NotekeaTheme(darkTheme = false) {
                    NotekeaPalette(
                        query = GIVEN,
                        onQueryChange = { queries += it },
                        placeholder = PLACEHOLDER,
                        onMove = {},
                        onConfirm = {},
                        onDismiss = {}
                    ) {}
                }
            }.use { scene ->
                repeat(times = SETTLE_FRAMES) { scene.render(nanoTime = it * FRAME_NANOS) }
                val typed = AwtKeyEvent(JPanel(), AwtKeyEvent.KEY_TYPED, 0L, 0, AwtKeyEvent.VK_UNDEFINED, TYPED)
                scene.sendKeyEvent(
                    event = KeyEvent(
                        key = Key.Unknown,
                        type = KeyEventType.Unknown,
                        codePoint = TYPED.code,
                        nativeEvent = typed
                    )
                )
                scene.render(nanoTime = SETTLE_FRAMES * FRAME_NANOS)
            }
        }

        assertEquals(
            expected = listOf(GIVEN + TYPED),
            actual = queries
        )
    }

    companion object {
        private const val DENSITY = 1.5f
        private const val FRAME_NANOS = 16_000_000L
        private const val GIVEN = "#trav"
        private const val PLACEHOLDER = "Search"
        private const val SETTLE_FRAMES = 3
        private const val TYPED = 'e'
        private const val WINDOW_HEIGHT = 480
        private const val WINDOW_WIDTH = 900
    }
}
