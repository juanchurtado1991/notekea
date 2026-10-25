package app.notekea.core.presentation.components

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.use
import app.notekea.core.presentation.theme.NotekeaTheme
import kotlin.test.Test

/** Read mode toggled quickly on a note with code blocks crashed with a color range past the end of a block (1.5 smoke). */
class NotekeaMarkdownCodeTest {
    @Test
    fun `code blocks colored at the same time never mix their texts`() {
        val isShown = mutableStateOf(value = true)
        ImageComposeScene(width = SCENE_WIDTH, height = SCENE_HEIGHT) {
            NotekeaTheme {
                if (isShown.value) NotekeaMarkdown(content = NOTE)
            }
        }.use { scene ->
            repeat(times = TOGGLES) { toggle ->
                isShown.value = toggle % 2 == 0
                // Several frames per toggle: the renderer parses, then every block is colored in the background.
                repeat(times = FRAMES_PER_TOGGLE) { frame ->
                    scene.render(nanoTime = (toggle * FRAMES_PER_TOGGLE + frame) * FRAME_NANOS)
                    Thread.sleep(COLORING_MILLIS)
                }
            }
        }
    }

    companion object {
        private const val COLORING_MILLIS = 2L
        private const val FRAMES_PER_TOGGLE = 4
        private const val FRAME_NANOS = 16_000_000L
        private const val SCENE_HEIGHT = 2400
        private const val SCENE_WIDTH = 800
        private const val TOGGLES = 60
        private val NOTE = (1..12).joinToString(separator = "\n\n") { block ->
            "```kotlin\nfun servings$block(people: Int): Int {\n    val perPerson = 2 // arepas per person\n" +
                "    return people * perPerson\n}\n```\n\n```python\ndef greet(name):\n" +
                "    return f\"Hello, {name}\"  # [[not a link inside code]]" + " ".repeat(n = block) + "\n```"
        }
    }
}
