package app.notekea.notes.presentation.editor

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.focus.FocusRequester
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
import app.notekea.notes.domain.OutlineFinder
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.SidePanelPreferences
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The side panel's outline beside the editor (`ROADMAP.md` 1.9): a click or Enter on a heading asks to go to it, the
 * editor scrolls a heading asked for to the top of its view in both modes, and the panel floats over a narrow window.
 */
class OutlineScreenTest {
    private val outline = OutlineFinder().headings(text = BODY)
    private var frame = 0L

    @Test
    fun `clicking a heading in the outline asks the editor to go to it`() {
        val intents = mutableListOf<EditorIntent>()
        scene(intents = intents) { scene, _, _ ->
            scene.sendPointerEvent(eventType = PointerEventType.Press, position = STEPS_ROW)
            scene.sendPointerEvent(eventType = PointerEventType.Release, position = STEPS_ROW)
            settle(scene = scene)
        }

        assertEquals(
            expected = listOf<EditorIntent>(EditorIntent.OnHeadingClick(index = 3)),
            actual = intents
        )
    }

    @Test
    fun `a heading asked for is scrolled to the top of the view, in edit and read mode, and becomes the current one`() {
        val currents = listOf(false, true).map { isReading ->
            var current = 0
            scene(isReading = isReading) { scene, _, tools ->
                tools.tracker.reveal(heading = outline[3])
                settle(scene = scene)
                // A little further down: still in Steps only if Steps was brought to the top.
                scene.sendPointerEvent(
                    eventType = PointerEventType.Scroll,
                    position = TEXT,
                    scrollDelta = Offset(x = 0f, y = 1f)
                )
                settle(scene = scene)
                current = tools.tracker.current
            }
            current
        }

        assertEquals(
            expected = listOf(3, 3),
            actual = currents
        )
    }

    @Test
    fun `the outline is moved with the keyboard from the current heading, Enter goes and Esc leaves`() {
        val intents = mutableListOf<EditorIntent>()
        scene(intents = intents) { scene, host, _ ->
            host.outlineFocus.requestFocus()
            settle(scene = scene)
            listOf(Key.DirectionDown, Key.DirectionDown, Key.Enter, Key.Escape).forEach { key ->
                scene.sendKeyEvent(event = keyDown(key = key))
                settle(scene = scene)
            }
        }

        assertEquals(
            expected = listOf(EditorIntent.OnHeadingClick(index = 2), EditorIntent.OnFocusRequest()),
            actual = intents
        )
    }

    @Test
    fun `the panel sits beside the editor while both fit, and floats over it, closed, when its narrowest does not`() {
        val layouts = listOf(WIDE, NARROW).map { width ->
            var layout = false to false
            scene(width = width) { _, host, _ -> layout = host.isOverlay to host.isOverlayOpen }
            layout
        }

        assertEquals(
            expected = listOf(false to false, true to false),
            actual = layouts
        )
    }

    private fun scene(
        isReading: Boolean = false,
        width: Int = WIDE,
        intents: MutableList<EditorIntent> = mutableListOf(),
        actions: (ImageComposeScene, SidePanelHost, EditorPaneTools) -> Unit
    ) {
        val host = SidePanelHost(send = { intents += it })
        val tools = EditorPaneTools()
        val state = EditorState(notePath = RelativePath(value = NOTE), isReading = isReading, outline = outline)
        val buffer = TextFieldState(initialText = BODY)
        UiThread.run {
            ImageComposeScene(width = width, height = HEIGHT, density = Density(density = 1f)) {
                NotekeaTheme(darkTheme = false) {
                    WithSidePanel(
                        preferences = SidePanelPreferences.DEFAULT,
                        sidePanel = host,
                        outline = state.outline,
                        current = tools.tracker.current,
                        platformConventions = PreviewPlatformConventions.linux,
                        onHeadingClick = { intents += EditorIntent.OnHeadingClick(index = it) },
                        onLeave = { intents += EditorIntent.OnFocusRequest() },
                        info = null
                    ) {
                        EditorContent(
                            state = state,
                            buffer = buffer,
                            platformConventions = PreviewPlatformConventions.linux,
                            onIntent = { intents += it },
                            textFocusRequester = FocusRequester(),
                            outlineTracker = tools.tracker
                        )
                    }
                }
            }.use { scene ->
                settle(scene = scene)
                actions(scene, host, tools)
            }
        }
    }

    /** A key pressed alone, as the window sends it. */
    @OptIn(InternalComposeUiApi::class)
    private fun keyDown(
        key: Key
    ): KeyEvent = KeyEvent(key = key, type = KeyEventType.KeyDown)

    private fun settle(
        scene: ImageComposeScene
    ) {
        // Read mode's Markdown is parsed away from the UI thread: frames alone do not wait for it.
        repeat(times = SETTLE_FRAMES) {
            frame += FRAME_NANOS
            scene.render(nanoTime = frame)
            Thread.sleep(PARSE_PAUSE_MILLIS)
        }
    }

    companion object {
        private const val FRAME_NANOS = 16_000_000L
        private const val HEIGHT = 650
        private const val NARROW = 700
        private const val NOTE = "Recipes/Arepas.md"
        private const val PARSE_PAUSE_MILLIS = 10L
        private const val SETTLE_FRAMES = 20
        private const val WIDE = 1100
        private val BODY = buildString {
            append("# Arepas\n\nA corn bread.\n\n## Ingredients\n\n- Corn flour\n\n### For the filling\n\n- Cheese\n\n")
            append("## Steps\n\n")
            repeat(times = 8) { append("Step ${it + 1}: knead the dough a little longer.\n\n") }
            append("### Cooking\n\n")
            repeat(times = 20) { append("More about cooking, line ${it + 1}.\n\n") }
        }
        private val STEPS_ROW = Offset(x = 850f, y = 172f)
        private val TEXT = Offset(x = 300f, y = 400f)
    }
}
