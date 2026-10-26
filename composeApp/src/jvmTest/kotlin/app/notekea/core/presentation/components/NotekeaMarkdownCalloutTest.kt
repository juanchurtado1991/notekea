package app.notekea.core.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.use
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.theme.NotekeaTheme
import org.jetbrains.skia.EncodedImageFormat
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Callouts in Read mode (`ROADMAP.md` 1.7), rendered headless: a folded callout opens and closes with a click on its
 * title (seen as a change in the drawn image), and the smoke note with every type, alias and nesting level renders.
 */
class NotekeaMarkdownCalloutTest {
    @Test
    fun `a folded callout opens and closes with a click on its title`() {
        val frames = mutableListOf<String>()
        UiThread.run {
            markdownScene(content = FOLDED_CALLOUT, height = SMALL_HEIGHT).use { scene ->
                settle(scene = scene)
                frames += image(scene = scene)
                repeat(times = 2) { click ->
                    // Clicks a second apart, as a person folds and unfolds; the pointer then leaves the title.
                    val time = (click + 1) * CLICK_GAP_MILLIS
                    scene.sendPointerEvent(
                        eventType = PointerEventType.Press,
                        position = CALLOUT_TITLE,
                        timeMillis = time
                    )
                    scene.sendPointerEvent(
                        eventType = PointerEventType.Release,
                        position = CALLOUT_TITLE,
                        timeMillis = time
                    )
                    scene.sendPointerEvent(eventType = PointerEventType.Move, position = OUTSIDE, timeMillis = time)
                    scene.render(nanoTime = time * NANOS_PER_MILLI)
                    frames += image(scene = scene)
                }
            }
        }

        assertNotEquals(
            illegal = frames[0],
            actual = frames[1],
            message = "Clicking the title did not open the callout"
        )
        assertEquals(expected = frames[0], actual = frames[2], message = "A second click did not fold it again")
    }

    @Test
    fun `the smoke note with every callout type, alias and nesting level renders`() {
        val note = File(SMOKE_NOTE).readText()
        UiThread.run {
            markdownScene(content = note, height = TALL_HEIGHT).use { scene -> settle(scene = scene) }
        }
    }

    private fun markdownScene(
        content: String,
        height: Int
    ): ImageComposeScene = ImageComposeScene(width = WIDTH, height = height, density = Density(density = DENSITY)) {
        NotekeaTheme(darkTheme = false) {
            Surface { Box(modifier = Modifier.padding(all = PADDING_DP.dp)) { NotekeaMarkdown(content = content) } }
        }
    }

    /** The renderer parses in the background, so a few frames with short pauses come first. */
    private fun settle(
        scene: ImageComposeScene
    ) {
        repeat(times = SETTLE_FRAMES) {
            scene.render(nanoTime = it * FRAME_NANOS)
            Thread.sleep(PARSE_PAUSE_MILLIS)
        }
    }

    private fun image(
        scene: ImageComposeScene
    ): String = scene.render(
        nanoTime = Long.MAX_VALUE / 2
    ).encodeToData(EncodedImageFormat.PNG)!!.bytes.contentHashCode().toString()

    companion object {
        private const val CLICK_GAP_MILLIS = 1_000L
        private const val DENSITY = 1.25f
        private const val NANOS_PER_MILLI = 1_000_000L
        private const val FOLDED_CALLOUT = "> [!faq]- A folded question\n> The hidden answer.\n"
        private const val FRAME_NANOS = 16_000_000L
        private const val PADDING_DP = 24
        private const val PARSE_PAUSE_MILLIS = 40L
        private const val SETTLE_FRAMES = 8
        private const val SMALL_HEIGHT = 300
        private const val SMOKE_NOTE = "../smoke-data/notes/Tasks and callouts.md"
        private const val TALL_HEIGHT = 4_200
        private const val WIDTH = 1_000
        private val CALLOUT_TITLE = Offset(x = 200f, y = 62f)
        private val OUTSIDE = Offset(x = 990f, y = 290f)
    }
}
