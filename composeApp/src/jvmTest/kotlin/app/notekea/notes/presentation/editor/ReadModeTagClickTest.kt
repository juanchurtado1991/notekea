package app.notekea.notes.presentation.editor

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import app.notekea.core.presentation.preview.PreviewPlatformConventions
import app.notekea.core.presentation.theme.NotekeaTheme
import app.notekea.notes.domain.RelativePath
import javax.swing.SwingUtilities
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A `#tag` in Read mode, rendered headless and clicked: it asks to search the tag. The renderer parses in the
 * background, so the scene renders a few frames with short pauses first; it runs on the AWT event thread, as in the
 * app (`ARCHITECTURE.md` §8).
 */
class ReadModeTagClickTest {
    @Test
    fun `clicking a tag in read mode asks to search it`() {
        val intents = mutableListOf<EditorIntent>()
        SwingUtilities.invokeAndWait {
            ImageComposeScene(width = WINDOW_WIDTH, height = WINDOW_HEIGHT, density = Density(density = DENSITY)) {
                NotekeaTheme(darkTheme = false) {
                    EditorContent(
                        state = EditorState(notePath = RelativePath(value = NOTE), isReading = true),
                        buffer = TextFieldState(initialText = BODY),
                        platformConventions = PreviewPlatformConventions.linux,
                        onIntent = { intents += it }
                    )
                }
            }.use { scene ->
                repeat(times = PARSE_FRAMES) { frame ->
                    scene.render(nanoTime = frame * FRAME_NANOS)
                    Thread.sleep(PARSE_PAUSE_MILLIS)
                }
                scene.sendPointerEvent(eventType = PointerEventType.Press, position = TAG_POSITION)
                scene.sendPointerEvent(eventType = PointerEventType.Release, position = TAG_POSITION)
                scene.render(nanoTime = PARSE_FRAMES * FRAME_NANOS)
            }
        }

        assertEquals(
            expected = listOf<EditorIntent>(EditorIntent.OnTagClick(tag = "recipe")),
            actual = intents
        )
    }

    companion object {
        private const val BODY = "# Arepas\n\nServe hot. #recipe\n"
        private const val DENSITY = 1.5f
        private const val FRAME_NANOS = 16_000_000L
        private const val NOTE = "Recipes/Arepas.md"
        private const val PARSE_FRAMES = 10
        private const val PARSE_PAUSE_MILLIS = 30L
        private const val WINDOW_HEIGHT = 500
        private const val WINDOW_WIDTH = 900
        private val TAG_POSITION = Offset(x = 225f, y = 235f)
    }
}
