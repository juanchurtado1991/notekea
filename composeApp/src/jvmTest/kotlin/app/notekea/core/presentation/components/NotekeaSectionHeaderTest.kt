package app.notekea.core.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.nodes
import app.notekea.core.presentation.texts
import app.notekea.core.presentation.theme.NotekeaTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A section header (`ROADMAP.md` 1.14) rendered headless and hovered: only a header with a shortcut has a tooltip,
 * since one repeating the title covered the section's first row (1.14 smoke).
 */
class NotekeaSectionHeaderTest {
    @Test
    fun `only a header with a shortcut shows a tooltip on hover`() {
        val hovered = listOf(BOOKMARKS, OUTLINE).map { title -> tooltipShown(title = title) }

        assertEquals(expected = listOf(false, true), actual = hovered)
    }

    private fun tooltipShown(
        title: String
    ): Boolean {
        var isShown = false
        UiThread.run {
            ImageComposeScene(width = WIDTH, height = HEIGHT, density = Density(density = 1f)) {
                NotekeaTheme(darkTheme = false) {
                    Column {
                        NotekeaSectionHeader(title = BOOKMARKS, onToggle = {})
                        NotekeaSectionHeader(title = OUTLINE, onToggle = {}, shortcutHint = SHORTCUT)
                    }
                }
            }.use { scene ->
                scene.render(nanoTime = 0L)
                val header = scene.nodes().first { node ->
                    node.config.getOrNull(SemanticsProperties.Text)?.any { it.text == title.uppercase() } == true
                }
                scene.sendPointerEvent(eventType = PointerEventType.Move, position = Offset(x = 1f, y = 1f))
                scene.sendPointerEvent(eventType = PointerEventType.Move, position = header.boundsInRoot.center)
                repeat(times = HOVER_FRAMES) { scene.render(nanoTime = (it + 1) * FRAME_NANOS) }
                isShown = scene.texts().any { it.startsWith(prefix = title) }
            }
        }
        return isShown
    }

    companion object {
        private const val BOOKMARKS = "Bookmarks"
        private const val FRAME_NANOS = 250_000_000L
        private const val HEIGHT = 300
        private const val HOVER_FRAMES = 8
        private const val OUTLINE = "Outline"
        private const val SHORTCUT = "Ctrl+Shift+O"
        private const val WIDTH = 400
    }
}
