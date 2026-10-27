package app.notekea.notes.presentation.editor

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.preview.PreviewPlatformConventions
import app.notekea.core.presentation.theme.NotekeaTheme
import app.notekea.notes.domain.FormatAction
import app.notekea.notes.domain.ListAction
import app.notekea.notes.domain.RelativePath
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The editor toolbar (`ROADMAP.md` 1.17), rendered headless on the AWT event thread and clicked where a person would:
 * buttons ask for their formatting, menus offer their choices, the tabs switch modes, and the toolbar stays in reach
 * when the note is scrolled. The click positions are those of a 1200 × 640 px window at 1.5 density.
 */
class EditorToolbarTest {
    @Test
    fun `buttons format, link to a note and insert template ask to choose one, and the tabs switch modes`() {
        val intents = clickAll(
            isReading = false,
            positions = listOf(BOLD, NOTE_LINK, CHECKLIST, TEMPLATE, EDIT_TAB, PREVIEW_TAB, FIND)
        )

        assertEquals(
            expected = listOf(
                EditorIntent.OnFormat(action = FormatAction.Bold(placeholder = "bold text")),
                EditorIntent.OnNoteLinkRequest,
                EditorIntent.OnFormat(action = FormatAction.Checklist(placeholder = "To do")),
                EditorIntent.OnTemplateRequest,
                EditorIntent.OnReadModeToggle,
                EditorIntent.OnFindOpen
            ),
            actual = intents
        )
    }

    @Test
    fun `code block writes the chosen language, and the table picked on the grid has that size`() {
        val intents =
            clickAll(isReading = false, positions = listOf(MORE, CODE_BLOCK, KOTLIN, MORE, TABLE_ROW, FOUR_BY_THREE))

        assertEquals(
            expected = listOf<EditorIntent>(
                EditorIntent.OnFormat(action = FormatAction.CodeBlock(placeholder = "code", language = "kotlin")),
                EditorIntent.OnFormat(
                    action = FormatAction.Table(
                        columnNames = listOf("Column 1", "Column 2", "Column 3", "Column 4"),
                        rows = 3,
                        cellText = "Text"
                    )
                )
            ),
            actual = intents
        )
    }

    @Test
    fun `preview keeps only the tabs, find and outline`() {
        val intents = clickAll(isReading = true, positions = listOf(BOLD, MORE, EDIT_TAB, FIND, OUTLINE))

        assertEquals(
            expected = listOf(EditorIntent.OnReadModeToggle, EditorIntent.OnFindOpen, EditorIntent.OnSidePanelToggle),
            actual = intents
        )
    }

    @Test
    fun `menus write a heading and a folded callout of the chosen type, and tag asks for the tag picker`() {
        val intents = clickAll(
            isReading = false,
            positions = listOf(
                HEADING,
                MEDIUM_HEADING,
                TAG,
                MORE,
                CALLOUT,
                FOLDED,
                TIP_WITH_FOLDED_CHECKED
            )
        )

        assertEquals(
            expected = listOf<EditorIntent>(
                EditorIntent.OnFormat(action = FormatAction.Heading(level = 2, placeholder = "Heading")),
                EditorIntent.OnTagRequest,
                EditorIntent.OnFormat(
                    action = FormatAction.Callout(
                        type = "tip",
                        title = "Tip",
                        placeholder = "Write here",
                        isFolded = true
                    )
                )
            ),
            actual = intents
        )
    }

    @Test
    fun `formatting shortcuts work in the text`() {
        val intents = mutableListOf<EditorIntent>()
        scene(isReading = false, intents = intents) { scene ->
            click(scene = scene, position = TEXT)
            listOf(Key.B, Key.I, Key.K).forEach { key ->
                scene.sendKeyEvent(event = ctrlKeyDown(key = key))
            }
            scene.sendKeyEvent(event = ctrlKeyDown(key = Key.K, isShiftPressed = true))
        }

        assertEquals(
            expected = listOf(
                EditorIntent.OnFormat(action = FormatAction.Bold(placeholder = "bold text")),
                EditorIntent.OnFormat(action = FormatAction.Italic(placeholder = "italic text")),
                EditorIntent.OnNoteLinkRequest,
                EditorIntent.OnFormat(action = FormatAction.WebLink(placeholder = "link text"))
            ),
            actual = intents.filterNot { it == EditorIntent.OnFocusLost }
        )
    }

    @Test
    fun `the toolbar stays at the top of a scrolled note, where its buttons still work`() {
        val intents = mutableListOf<EditorIntent>()
        scene(isReading = false, intents = intents) { scene ->
            scene.sendPointerEvent(
                eventType = PointerEventType.Scroll,
                position = TEXT,
                scrollDelta = Offset(x = 0f, y = SCROLL_TICKS)
            )
            settle(scene = scene)
            click(scene = scene, position = BOLD.copy(y = STUCK_BUTTON_Y))
        }

        assertEquals(
            expected = listOf<EditorIntent>(
                EditorIntent.OnFormat(action = FormatAction.Bold(placeholder = "bold text"))
            ),
            actual = intents
        )
    }

    @Test
    fun `mouse-selected lines stay selected for a button, a menu and the tabs, which leave the keyboard in the text`() {
        val intents = mutableListOf<EditorIntent>()
        val buffer = TextFieldState(initialText = BODY)
        val selections = mutableListOf<String>()
        scene(isReading = false, intents = intents, buffer = buffer) { scene ->
            drag(scene = scene, from = TEXT.copy(x = TEXT_LEFT), to = THIRD_STEP)
            click(scene = scene, position = CHECKLIST)
            selections += buffer.text.substring(range = buffer.selection.min..<buffer.selection.max)
            click(scene = scene, position = MORE)
            selections += buffer.text.substring(range = buffer.selection.min..<buffer.selection.max)
            click(scene = scene, position = TOOLBAR_GAP)
            click(scene = scene, position = PREVIEW_TAB)
        }

        assertEquals(
            expected = listOf(
                "Step 1 of the recipe.\nStep 2 of the recipe.\nStep ",
                "Step 1 of the recipe.\nStep 2 of the recipe.\nStep "
            ),
            actual = selections
        )
        assertEquals(
            expected = listOf<EditorIntent>(
                EditorIntent.OnFormat(action = FormatAction.Checklist(placeholder = "To do")),
                EditorIntent.OnReadModeToggle
            ),
            actual = intents
        )
    }

    @Test
    fun `clicking in the text does not scroll the note, so the toolbar stays where it was`() {
        val intents = mutableListOf<EditorIntent>()
        scene(isReading = false, intents = intents) { scene ->
            click(scene = scene, position = THIRD_STEP)
            click(scene = scene, position = BOLD)
        }

        assertEquals(
            expected = listOf<EditorIntent>(
                EditorIntent.OnFormat(action = FormatAction.Bold(placeholder = "bold text"))
            ),
            actual = intents
        )
    }

    @Test
    fun `enter, tab and backspace in a list item edit the list`() {
        val intents = mutableListOf<EditorIntent>()
        scene(
            isReading = false,
            intents = intents,
            buffer = TextFieldState(initialText = "# Arepas\n\n- Flour\n- Salt")
        ) { scene ->
            click(scene = scene, position = TEXT.copy(x = LINE_END_X))
            listOf(Key.Enter, Key.Tab).forEach { scene.sendKeyEvent(event = keyDown(key = it)) }
            scene.sendKeyEvent(event = keyDown(key = Key.Tab, isShiftPressed = true))
            scene.sendKeyEvent(event = keyDown(key = Key.Backspace))
        }

        assertEquals(
            expected = listOf(
                ListAction.NEW_ITEM,
                ListAction.INDENT,
                ListAction.OUTDENT
            ).map<ListAction, EditorIntent> {
                EditorIntent.OnListKey(action = it)
            },
            actual = intents
        )
    }

    private fun drag(
        scene: ImageComposeScene,
        from: Offset,
        to: Offset
    ) {
        scene.sendPointerEvent(eventType = PointerEventType.Move, position = from)
        scene.sendPointerEvent(eventType = PointerEventType.Press, position = from)
        settle(scene = scene)
        scene.sendPointerEvent(eventType = PointerEventType.Move, position = to)
        settle(scene = scene)
        scene.sendPointerEvent(eventType = PointerEventType.Release, position = to)
        settle(scene = scene)
    }

    /** A key pressed without Ctrl, as the window sends it. */
    @OptIn(InternalComposeUiApi::class)
    private fun keyDown(
        key: Key,
        isShiftPressed: Boolean = false
    ): KeyEvent = KeyEvent(key = key, type = KeyEventType.KeyDown, isShiftPressed = isShiftPressed)

    private fun clickAll(
        isReading: Boolean,
        positions: List<Offset>
    ): List<EditorIntent> {
        val intents = mutableListOf<EditorIntent>()
        scene(isReading = isReading, intents = intents) { scene ->
            positions.forEach { click(scene = scene, position = it) }
        }
        return intents
    }

    private fun scene(
        isReading: Boolean,
        intents: MutableList<EditorIntent>,
        buffer: TextFieldState = TextFieldState(initialText = BODY),
        actions: (ImageComposeScene) -> Unit
    ) {
        UiThread.run {
            ImageComposeScene(width = WINDOW_WIDTH, height = WINDOW_HEIGHT, density = Density(density = DENSITY)) {
                NotekeaTheme(darkTheme = false) {
                    EditorContent(
                        state = EditorState(notePath = RelativePath(value = NOTE), isReading = isReading),
                        buffer = buffer,
                        platformConventions = PreviewPlatformConventions.linux,
                        onIntent = { intents += it },
                        properties = EditorPreviewData.properties
                    )
                }
            }.use { scene ->
                settle(scene = scene)
                actions(scene)
            }
        }
        // Closing the scene takes the keyboard from the text; only what happened before matters.
        if (intents.lastOrNull() == EditorIntent.OnFocusLost) intents.removeAt(index = intents.lastIndex)
    }

    /** A key pressed with Ctrl, and Shift when [isShiftPressed], as the window sends it. */
    @OptIn(InternalComposeUiApi::class)
    private fun ctrlKeyDown(
        key: Key,
        isShiftPressed: Boolean = false
    ): KeyEvent = KeyEvent(
        key = key,
        type = KeyEventType.KeyDown,
        isCtrlPressed = true,
        isShiftPressed = isShiftPressed
    )

    private fun click(
        scene: ImageComposeScene,
        position: Offset
    ) {
        scene.sendPointerEvent(eventType = PointerEventType.Move, position = position)
        scene.sendPointerEvent(eventType = PointerEventType.Press, position = position)
        scene.sendPointerEvent(eventType = PointerEventType.Release, position = position)
        settle(scene = scene)
    }

    /** Renders frames far enough apart for menus to open and close, which animate. */
    private fun settle(
        scene: ImageComposeScene
    ) {
        repeat(times = SETTLE_FRAMES) {
            frame += MENU_FRAME_NANOS
            scene.render(nanoTime = frame)
        }
    }

    private var frame = 0L

    companion object {
        private const val DENSITY = 1.5f
        private const val MENU_FRAME_NANOS = 160_000_000L
        private const val NOTE = "Recipes/Arepas.md"
        private const val SCROLL_TICKS = 20f
        private const val SETTLE_FRAMES = 4
        private const val STUCK_BUTTON_Y = 48f
        private const val WINDOW_HEIGHT = 900
        private const val WINDOW_WIDTH = 1200
        private val BODY = "# Arepas\n\n" + (1..40).joinToString(separator = "") { "Step $it of the recipe.\n" }
        private val BOLD = Offset(x = 601f, y = 161f)
        private val CALLOUT = Offset(x = 800f, y = 350f)
        private val CHECKLIST = Offset(x = 821f, y = 161f)
        private val EDIT_TAB = Offset(x = 115f, y = 168f)
        private val FIND = Offset(x = 1059f, y = 161f)
        private val OUTLINE = Offset(x = 1101f, y = 161f)
        private val FOLDED = Offset(x = 300f, y = 190f)
        private val HEADING = Offset(x = 553f, y = 161f)
        private val MEDIUM_HEADING = Offset(x = 634f, y = 267f)
        private val NOTE_LINK = Offset(x = 711f, y = 161f)
        private val PREVIEW_TAB = Offset(x = 211f, y = 168f)
        private val TEMPLATE = Offset(x = 931f, y = 161f)
        private val TAG = Offset(x = 759f, y = 161f)
        private val CODE_BLOCK = Offset(x = 800f, y = 419f)
        private val FOUR_BY_THREE = Offset(x = 436f, y = 738f)
        private val KOTLIN = Offset(x = 460f, y = 624f)
        private val MORE = Offset(x = 993f, y = 161f)
        private val TABLE_ROW = Offset(x = 800f, y = 729f)
        private const val LINE_END_X = 700f
        private const val TEXT_LEFT = 78f
        private val TOOLBAR_GAP = Offset(x = 290f, y = 161f)
        private val THIRD_STEP = Offset(x = 140f, y = 377f)
        private val TEXT = Offset(x = 300f, y = 305f)
        private val TIP_WITH_FOLDED_CHECKED = Offset(x = 300f, y = 480f)
    }
}
