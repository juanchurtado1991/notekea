package app.notekea.core.presentation.designsystem.components

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.use
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.designsystem.components.markdown.NotekeaMarkdown
import app.notekea.core.presentation.designsystem.components.markdown.TextMarks
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Marks painted behind rendered Markdown, as Find's matches in the editor's drawn blocks: they follow the state their
 * ranges read, with no new layout of the text. Rendered headless on the AWT event thread ([UiThread]).
 */
class TextMarksTest {
    @Test
    fun `marks are painted behind the words they name, and follow the state they read`() {
        val word = mutableStateOf(value = "corn")

        val marks = TextMarks(color = Color.Red) { text ->
            listOf(text.indexOf(string = word.value))
                .filter {
                    word.value.isNotEmpty() && it >= 0
                }
                .map {
                    it until it + word.value.length
                }
        }

        UiThread.run {
            ImageComposeScene(
                width = SCENE_WIDTH,
                height = SCENE_HEIGHT
            ) {
                NotekeaTheme(darkTheme = false) {
                    NotekeaMarkdown(
                        content = NOTE,
                        marks = marks
                    )
                }
            }.use { scene ->
                val corn = marked(scene = scene)

                word.value = "Mix the corn flour"
                val sentence = marked(scene = scene)

                word.value = ""
                val none = marked(scene = scene)

                assertTrue(
                    actual = corn > 0,
                    message = "No mark was painted"
                )

                assertTrue(
                    actual = sentence > corn * 2,
                    message = "The marks did not follow their state: $corn, then $sentence"
                )

                assertEquals(
                    expected = 0,
                    actual = none
                )
            }
        }
    }

    /** How many pixels of the scene are the marks' color. */
    private fun marked(scene: ImageComposeScene): Int {
        val pixels = IntArray(size = SCENE_WIDTH * SCENE_HEIGHT)

        repeat(times = SETTLE_FRAMES) { scene.render(nanoTime = (frame++) * FRAME_NANOS) }

        scene
            .render(nanoTime = (frame++) * FRAME_NANOS)
            .toComposeImageBitmap()
            .readPixels(buffer = pixels)

        return pixels.count {
            Color(color = it) == Color.Red
        }
    }

    private var frame = 0L

    companion object {
        private const val FRAME_NANOS = 16_000_000L
        private const val NOTE = "# Arepas\n\nMix the corn flour.\n\n- Rest the dough."
        private const val SCENE_HEIGHT = 300
        private const val SCENE_WIDTH = 600
        private const val SETTLE_FRAMES = 3
    }
}
