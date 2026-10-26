package app.notekea.notes.presentation.tags

import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.theme.NotekeaTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/** The tag picker palette, rendered headless on the AWT event thread: a click on a row adds that tag. */
class TagPickerContentTest {
    @Test
    fun `clicking a tag or the new tag row chooses it`() {
        val intents = mutableListOf<TagPickerIntent>()
        UiThread.run {
            ImageComposeScene(width = WINDOW_WIDTH, height = WINDOW_HEIGHT, density = Density(density = DENSITY)) {
                NotekeaTheme(darkTheme = false) {
                    TagPickerContent(
                        state = TagPickerState(
                            isOpen = true,
                            query = "rec",
                            rows = listOf(TagPickerRow(name = "recipe"), TagPickerRow(name = "rec", isNew = true))
                        ),
                        onIntent = { if (it is TagPickerIntent.OnRowClick) intents += it }
                    )
                }
            }.use { scene ->
                repeat(times = SETTLE_FRAMES) { scene.render(nanoTime = it * FRAME_NANOS) }
                listOf(FIRST_ROW, SECOND_ROW).forEach { position ->
                    scene.sendPointerEvent(eventType = PointerEventType.Press, position = position)
                    scene.sendPointerEvent(eventType = PointerEventType.Release, position = position)
                    scene.render(nanoTime = SETTLE_FRAMES * FRAME_NANOS)
                }
            }
        }

        assertEquals(
            expected = listOf<TagPickerIntent>(
                TagPickerIntent.OnRowClick(index = 0),
                TagPickerIntent.OnRowClick(index = 1)
            ),
            actual = intents
        )
    }

    companion object {
        private const val DENSITY = 1.5f
        private const val FRAME_NANOS = 16_000_000L
        private const val SETTLE_FRAMES = 3
        private const val WINDOW_HEIGHT = 640
        private const val WINDOW_WIDTH = 1200
        private val FIRST_ROW = Offset(x = 600f, y = 200f)
        private val SECOND_ROW = Offset(x = 600f, y = 260f)
    }
}
