package app.notekea.editor.presentation.view

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.designsystem.preview.PreviewPlatformConventions
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import app.notekea.editor.presentation.model.EditorIntent
import app.notekea.editor.presentation.model.EditorState
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tasks in Edit mode: a checkbox over each `[ ]` toggles that task; clicking the text does not. The tasks of the
 * blocks the cursor is not in are drawn by the renderer, and their checkboxes toggle them the same way.
 */
@OptIn(ExperimentalTestApi::class)
class EditorTaskCheckboxTest {
    @Test
    fun `a task's checkbox in a block the cursor is not in toggles that task`() = runComposeUiTest {
        val toggles = mutableListOf<Int>()

        setContent {
            NotekeaTheme(darkTheme = false) {
                EditorContent(
                    state = EditorState(notePath = RelativePath(value = NOTE)),
                    buffer = TextFieldState(
                        initialText = DRAWN_BODY,
                        initialSelection = TextRange(index = 0)
                    ),
                    platformConventions = PreviewPlatformConventions.linux,
                    onIntent = {
                        if (it is EditorIntent.OnTaskToggle) toggles += it.stateIndex
                    }
                )
            }
        }

        val boxes = onAllNodes(matcher = hasContentDescription(value = "Task"))

        assertEquals(
            expected = listOf("not checked", "checked", "not checked", "checked"),
            actual = boxes.fetchSemanticsNodes().map {
                it.config.getOrNull(key = SemanticsProperties.StateDescription)
            }
        )

        repeat(times = DRAWN_TASKS.size) { boxes[it].performClick() }

        assertEquals(
            expected = DRAWN_TASKS.map {
                DRAWN_BODY.indexOf(string = it) + 1
            },
            actual = toggles
        )
    }

    @Test
    fun `a checkbox of the lines the cursor is in toggles its task and leaves the cursor in the text`() =
        runComposeUiTest {
            val toggles = mutableListOf<Int>()

            setContent {
                NotekeaTheme(darkTheme = false) {
                    EditorContent(
                        state = EditorState(notePath = RelativePath(value = NOTE)),
                        buffer = TextFieldState(
                            initialText = BODY,
                            initialSelection = TextRange(index = BODY.indexOf(string = "Buy"))
                        ),
                        platformConventions = PreviewPlatformConventions.linux,
                        onIntent = {
                            if (it is EditorIntent.OnTaskToggle) toggles += it.stateIndex
                        }
                    )
                }
            }

            // With the text focused the cursor's line shows its marks: moving the focus to the box would hide
            // them again and take the box from under the pointer before the click ends.
            onAllNodes(matcher = hasSetTextAction())[1].requestFocus()
            onAllNodes(matcher = hasSetTextAction())[1].assertIsFocused()

            onAllNodes(matcher = hasContentDescription(value = "Task"))[0].performClick()

            assertEquals(
                expected = listOf(BODY.indexOf(string = "[ ] Buy flour") + 1),
                actual = toggles
            )

            onAllNodes(matcher = hasSetTextAction())[1].assertIsFocused()
        }

    @Test
    fun `clicking a task's checkbox in edit mode toggles that task, clicking its text does not`() {
        val toggles = mutableListOf<EditorIntent.OnTaskToggle>()

        UiThread.run {
            ImageComposeScene(
                width = WINDOW_WIDTH,
                height = WINDOW_HEIGHT,
                density = Density(density = DENSITY)
            ) {
                NotekeaTheme(darkTheme = false) {
                    EditorContent(
                        state = EditorState(notePath = RelativePath(value = NOTE)),
                        buffer = TextFieldState(initialText = BODY),
                        platformConventions = PreviewPlatformConventions.linux,
                        onIntent = {
                            if (it is EditorIntent.OnTaskToggle) toggles += it
                        }
                    )
                }
            }.use { scene ->
                // The checkboxes follow the text's layout, which exists from the second frame.
                repeat(times = SETTLE_FRAMES) { scene.render(nanoTime = it * FRAME_NANOS) }

                listOf(FLOUR_CHECKBOX, FLOUR_TEXT).forEachIndexed { index, position ->
                    scene.sendPointerEvent(
                        eventType = PointerEventType.Press,
                        position = position
                    )

                    scene.sendPointerEvent(
                        eventType = PointerEventType.Release,
                        position = position
                    )

                    scene.render(nanoTime = (SETTLE_FRAMES + index) * FRAME_NANOS)
                }
            }
        }

        assertEquals(
            expected = listOf(
                EditorIntent.OnTaskToggle(
                    stateIndex =
                    BODY.indexOf(string = "[ ] Buy flour") + 1
                )
            ),
            actual = toggles
        )
    }

    companion object {
        private val DRAWN_TASKS =
            listOf("[ ] Buy flour", "[X] Soak the beans", "[ ] Call the baker", "[x] Pay")
        private const val DRAWN_BODY =
            "# Groceries\n\n- [ ] Buy flour\n- [X] Soak the beans\n\n## Later\n\nSoon.\n\n" +
                "> [!todo] To do\n> - [ ] Call the baker\n\n1. [x] Pay\n"
        private const val BODY = "# Groceries\n\n- [ ] Buy flour\n- [x] Soak the beans\n"
        private const val DENSITY = 1.5f
        private const val FRAME_NANOS = 16_000_000L
        private const val SETTLE_FRAMES = 3
        private const val NOTE = "Projects/Groceries.md"
        private const val WINDOW_HEIGHT = 520
        private const val WINDOW_WIDTH = 900
        private val FLOUR_CHECKBOX = Offset(
            x = 62f,
            y = 356f
        )
        private val FLOUR_TEXT = Offset(
            x = 160f,
            y = 344f
        )
    }
}
