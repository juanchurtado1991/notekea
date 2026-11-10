package app.notekea.editor.presentation.view

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerKeyboardModifiers
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.use
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.designsystem.preview.PreviewPlatformConventions
import app.notekea.core.presentation.designsystem.theme.NotekeaTheme
import app.notekea.editor.presentation.model.EditorState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The editor's page is a list of the note's blocks, and only the cursor's block is a text field: clicks, keys and
 * drags move the cursor and the selection from one block to another as if the note were one text. Rendered headless
 * on the AWT event thread; positions are those of a 900 × 600 px window.
 */
@OptIn(InternalComposeUiApi::class)
class EditorBlocksScreenTest {
    private var frame = 0L

    @Test
    fun `a click on a drawn block puts the cursor in it, and a shift click selects up to another`() {
        val buffer = buffer(cursor = 0)

        render(buffer = buffer) { scene, _ ->
            click(
                scene = scene,
                position = REST_LINE
            )

            assertTrue(
                actual = buffer.selection.collapsed && buffer.selection.end in rangeOf(part = REST),
                message = "The cursor is at ${buffer.selection}"
            )

            val anchor = buffer.selection.start

            click(
                scene = scene,
                position = SERVE_LINE,
                isShiftPressed = true
            )

            assertEquals(
                expected = anchor,
                actual = buffer.selection.start
            )

            assertTrue(
                actual = buffer.selection.end in rangeOf(part = SERVE),
                message = "The selection is ${buffer.selection}"
            )
        }
    }

    @Test
    fun `the arrows cross from one block to the next and back`() {
        val mix = BODY.indexOf(string = MIX)
        val buffer = buffer(cursor = mix + MIX.length)

        render(buffer = buffer) { scene, focus ->
            focus.requestFocus()
            settle(scene = scene)

            // The block keeps the blank line after it: one arrow to it, another out of the block.
            listOf(Key.DirectionRight, Key.DirectionRight).forEach {
                press(
                    scene = scene,
                    key = it
                )
            }

            assertEquals(
                expected = TextRange(index = BODY.indexOf(string = REST)),
                actual = buffer.selection
            )

            press(
                scene = scene,
                key = Key.DirectionLeft
            )

            assertEquals(
                expected = TextRange(index = BODY.indexOf(string = REST) - 1),
                actual = buffer.selection
            )

            press(
                scene = scene,
                key = Key.DirectionDown
            )

            assertEquals(
                expected = TextRange(index = BODY.indexOf(string = REST)),
                actual = buffer.selection
            )

            press(
                scene = scene,
                key = Key.DirectionUp
            )

            assertEquals(
                expected = TextRange(index = BODY.indexOf(string = REST) - 1),
                actual = buffer.selection
            )

            press(
                scene = scene,
                key = Key.DirectionDown,
                isShiftPressed = true
            )

            assertEquals(
                expected = TextRange(
                    start = BODY.indexOf(string = REST) - 1,
                    end = BODY.indexOf(string = REST)
                ),
                actual = buffer.selection
            )
        }
    }

    @Test
    fun `backspace at a block's start and delete at its end join it with its neighbour`() {
        val rest = BODY.indexOf(string = REST)
        val buffer = buffer(cursor = rest)

        render(buffer = buffer) { scene, focus ->
            focus.requestFocus()
            settle(scene = scene)

            press(
                scene = scene,
                key = Key.Backspace
            )

            assertEquals(
                expected = BODY.replace(
                    oldValue = "\n\n$REST",
                    newValue = "\n$REST"
                ),
                actual = buffer.text.toString()
            )

            assertEquals(
                expected = TextRange(index = rest - 1),
                actual = buffer.selection
            )

            // The heading's block ends before its blank line's break, which Delete takes.
            buffer.edit { selection = TextRange(index = "# Arepas\n".length) }
            settle(scene = scene)

            press(
                scene = scene,
                key = Key.Delete
            )

            assertTrue(
                actual = buffer.text.startsWith(prefix = "# Arepas\n$MIX"),
                message = "The text is ${buffer.text}"
            )
        }
    }

    @Test
    fun `a selection dragged out of the text goes on over the blocks below`() {
        val buffer = buffer(cursor = 0)

        render(buffer = buffer) { scene, _ ->
            listOf(
                PointerEventType.Move to HEADING_LINE,
                PointerEventType.Press to HEADING_LINE,
                PointerEventType.Move to HEADING_LINE.copy(x = HEADING_LINE.x + DRAG_START_PX),
                PointerEventType.Move to MIX_LINE,
                PointerEventType.Move to REST_LINE
            ).forEach { (type, position) ->
                scene.sendPointerEvent(
                    eventType = type,
                    position = position
                )

                settle(scene = scene)
            }

            scene.sendPointerEvent(
                eventType = PointerEventType.Release,
                position = REST_LINE
            )

            settle(scene = scene)

            assertTrue(
                actual = buffer.selection.start in rangeOf(part = "# Arepas") &&
                    buffer.selection.end in rangeOf(part = REST),
                message = "The selection is ${buffer.selection}"
            )
        }
    }

    @Test
    fun `the keyboard reaches a cursor whose block is far below what the page shows`() {
        val long = List(size = LONG_BLOCKS) { "Paragraph $it." }.joinToString(separator = "\n\n")
        val buffer = TextFieldState(initialText = long)

        render(buffer = buffer) { scene, focus ->
            focus.requestFocus()
            settle(scene = scene)

            press(
                scene = scene,
                key = Key.Backspace
            )

            assertEquals(
                expected = long.dropLast(n = 1),
                actual = buffer.text.toString()
            )
        }
    }

    private fun buffer(cursor: Int) = TextFieldState(
        initialText = BODY,
        initialSelection = TextRange(index = cursor)
    )

    private fun rangeOf(part: String): IntRange = BODY.indexOf(string = part).let {
        it..it + part.length
    }

    private fun render(
        buffer: TextFieldState,
        actions: (ImageComposeScene, FocusRequester) -> Unit
    ) {
        val focus = FocusRequester()

        UiThread.run {
            ImageComposeScene(
                width = WINDOW_WIDTH,
                height = WINDOW_HEIGHT
            ) {
                NotekeaTheme(darkTheme = false) {
                    EditorContent(
                        state = EditorState(notePath = RelativePath(value = NOTE)),
                        buffer = buffer,
                        platformConventions = PreviewPlatformConventions.linux,
                        onIntent = {},
                        textFocusRequester = focus
                    )
                }
            }.use { scene ->
                settle(scene = scene)

                actions(scene, focus)
            }
        }
    }

    private fun click(
        scene: ImageComposeScene,
        position: Offset,
        isShiftPressed: Boolean = false
    ) {
        listOf(PointerEventType.Move, PointerEventType.Press, PointerEventType.Release).forEach {
            scene.sendPointerEvent(
                eventType = it,
                position = position,
                keyboardModifiers = PointerKeyboardModifiers(isShiftPressed = isShiftPressed)
            )
        }

        settle(scene = scene)
    }

    private fun press(
        scene: ImageComposeScene,
        key: Key,
        isShiftPressed: Boolean = false
    ) {
        listOf(KeyEventType.KeyDown, KeyEventType.KeyUp).forEach {
            scene.sendKeyEvent(
                event = KeyEvent(
                    key = key,
                    type = it,
                    isShiftPressed = isShiftPressed
                )
            )
        }

        settle(scene = scene)
    }

    private fun settle(scene: ImageComposeScene) {
        repeat(times = SETTLE_FRAMES) {
            frame += FRAME_NANOS
            scene.render(nanoTime = frame)
        }
    }

    companion object {
        private const val MIX = "Mix the corn flour."
        private const val REST = "Rest the dough."
        private const val SERVE = "Serve hot."
        private const val BODY = "# Arepas\n\n$MIX\n\n$REST\n\n$SERVE"
        private const val DRAG_START_PX = 30f
        private const val FRAME_NANOS = 160_000_000L
        private const val LONG_BLOCKS = 200
        private const val NOTE = "Recipes/Arepas.md"
        private const val SETTLE_FRAMES = 4
        private const val WINDOW_HEIGHT = 600
        private const val WINDOW_WIDTH = 900
        private val HEADING_LINE = Offset(
            x = 140f,
            y = 174f
        )
        private val MIX_LINE = Offset(
            x = 140f,
            y = 236f
        )
        private val REST_LINE = Offset(
            x = 140f,
            y = 290f
        )
        private val SERVE_LINE = Offset(
            x = 140f,
            y = 344f
        )
    }
}
