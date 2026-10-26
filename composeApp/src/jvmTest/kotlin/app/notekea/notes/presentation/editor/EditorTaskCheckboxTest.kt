package app.notekea.notes.presentation.editor

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.preview.PreviewPlatformConventions
import app.notekea.core.presentation.theme.NotekeaTheme
import app.notekea.notes.domain.RelativePath
import kotlin.test.Test
import kotlin.test.assertEquals

/** Tasks in Edit mode (`ROADMAP.md` 1.7): a checkbox over each `[ ]` toggles that task; clicking the text does not. */
class EditorTaskCheckboxTest {
    @Test
    fun `clicking a task's checkbox in edit mode toggles that task, clicking its text does not`() {
        val toggles = mutableListOf<EditorIntent.OnTaskToggle>()
        UiThread.run {
            ImageComposeScene(width = WINDOW_WIDTH, height = WINDOW_HEIGHT, density = Density(density = DENSITY)) {
                NotekeaTheme(darkTheme = false) {
                    EditorContent(
                        state = EditorState(notePath = RelativePath(value = NOTE)),
                        buffer = TextFieldState(initialText = BODY),
                        platformConventions = PreviewPlatformConventions.linux,
                        onIntent = { if (it is EditorIntent.OnTaskToggle) toggles += it }
                    )
                }
            }.use { scene ->
                // The checkboxes follow the text's layout, which exists from the second frame.
                repeat(times = SETTLE_FRAMES) { scene.render(nanoTime = it * FRAME_NANOS) }
                listOf(FLOUR_CHECKBOX, FLOUR_TEXT).forEachIndexed { index, position ->
                    scene.sendPointerEvent(eventType = PointerEventType.Press, position = position)
                    scene.sendPointerEvent(eventType = PointerEventType.Release, position = position)
                    scene.render(nanoTime = (SETTLE_FRAMES + index) * FRAME_NANOS)
                }
            }
        }

        assertEquals(
            expected = listOf(EditorIntent.OnTaskToggle(stateIndex = BODY.indexOf(string = "[ ] Buy flour") + 1)),
            actual = toggles
        )
    }

    companion object {
        private const val BODY = "# Groceries\n\n- [ ] Buy flour\n- [x] Soak the beans\n"
        private const val DENSITY = 1.5f
        private const val FRAME_NANOS = 16_000_000L
        private const val SETTLE_FRAMES = 3
        private const val NOTE = "Projects/Groceries.md"
        private const val WINDOW_HEIGHT = 520
        private const val WINDOW_WIDTH = 900
        private val FLOUR_CHECKBOX = Offset(x = 80f, y = 247f)
        private val FLOUR_TEXT = Offset(x = 160f, y = 247f)
    }
}
