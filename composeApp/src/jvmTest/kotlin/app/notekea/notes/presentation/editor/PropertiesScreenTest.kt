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
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.preview.PreviewPlatformConventions
import app.notekea.core.presentation.theme.NotekeaTheme
import app.notekea.notes.domain.FrontmatterContent
import app.notekea.notes.domain.FrontmatterReader
import app.notekea.notes.domain.PropertyValue
import app.notekea.notes.domain.RelativePath
import javax.swing.JPanel
import kotlin.test.Test
import kotlin.test.assertEquals
import java.awt.event.KeyEvent as AwtKeyEvent

/**
 * The properties under a note's title (`ROADMAP.md` 1.14), rendered headless: the header folds them, a checkbox, a
 * chip's ✕ and a row's ✕ edit at once, typed text is committed with Enter, Show source swaps the form, and the
 * calendar (above the date when there is no room below) picks a day, today, or a day of another month.
 */
class PropertiesScreenTest {
    private var frame = 0L

    @Test
    fun `the form folds, edits each kind of value, removes a property, shows its source and moves to the panel`() {
        val intents = mutableListOf<EditorIntent>()
        UiThread.run {
            scene(intents = intents).use { scene ->
                settle(scene = scene)
                listOf(FAVORITE_BOX, RECIPE_REMOVE, STATUS_REMOVE, STATUS_FIELD_END).forEach { click(scene, it) }
                type(scene = scene, text = TYPED)
                scene.sendKeyEvent(event = keyDown(key = Key.Enter))
                settle(scene = scene)
                listOf(SOURCE_TOGGLE, TO_PANEL, HEADER).forEach { click(scene, it) }
            }
        }

        assertEquals(
            expected = listOf(
                PropertyIntent.OnValueChange(key = "favorite", value = PropertyValue.Checkbox(isChecked = false)),
                PropertyIntent.OnValueChange(key = "tags", value = PropertyValue.Items(items = listOf("venezuelan"))),
                PropertyIntent.OnRemove(key = "status"),
                PropertyIntent.OnValueChange(key = "status", value = PropertyValue.Text(text = "Tested$TYPED")),
                PropertyIntent.OnSourceToggle,
                EditorIntent.OnPropertiesPlacementToggle,
                PropertyIntent.OnFoldToggle
            ),
            actual = intents.filter { it is PropertyIntent || it == EditorIntent.OnPropertiesPlacementToggle }
        )
    }

    @Test
    fun `the calendar picks a day, today and a day of the next month`() {
        val intents = mutableListOf<EditorIntent>()
        UiThread.run {
            scene(intents = intents).use { scene ->
                settle(scene = scene)
                listOf(CALENDAR, OCTOBER_14, CALENDAR, TODAY, CALENDAR, NEXT_MONTH, NOVEMBER_2).forEach {
                    click(scene, it)
                }
            }
        }

        assertEquals(
            expected = listOf(
                CalendarDate(year = 2026, month = 10, day = 14),
                COOKED_TODAY,
                CalendarDate(year = 2026, month = 11, day = 2)
            ).map { PropertyIntent.OnValueChange(key = "cooked", value = PropertyValue.Date(date = it)) },
            actual = intents.filterIsInstance<PropertyIntent>()
        )
    }

    @Test
    fun `typing in the source sends each change, and clicking the note's text commits it`() {
        val intents = mutableListOf<EditorIntent>()
        val state = EditorState(
            notePath = RelativePath(value = NOTE),
            properties = FrontmatterContent.Invalid(line = 2),
            propertiesSource = BROKEN_SOURCE
        )
        UiThread.run {
            scene(intents = intents, state = state).use { scene ->
                settle(scene = scene)
                click(scene = scene, position = BROKEN_SOURCE_END)
                type(scene = scene, text = TYPED)
                click(scene = scene, position = BODY_TEXT)
            }
        }

        assertEquals(
            expected = listOf(
                PropertyIntent.OnSourceEdit(path = RelativePath(value = NOTE), yaml = BROKEN_SOURCE + TYPED),
                PropertyIntent.OnSourceCommit(path = RelativePath(value = NOTE), yaml = BROKEN_SOURCE + TYPED)
            ),
            actual = intents.filterIsInstance<PropertyIntent>()
        )
    }

    private fun scene(
        intents: MutableList<EditorIntent>,
        state: EditorState = EditorState(
            notePath = RelativePath(value = NOTE),
            properties = FrontmatterReader().read(header = HEADER_TEXT),
            today = COOKED_TODAY
        )
    ) = ImageComposeScene(width = WIDTH, height = HEIGHT, density = Density(density = 1f)) {
        NotekeaTheme(darkTheme = false) {
            EditorContent(
                state = state,
                buffer = TextFieldState(initialText = BODY),
                platformConventions = PreviewPlatformConventions.linux,
                onIntent = { intents += it }
            )
        }
    }

    private fun click(
        scene: ImageComposeScene,
        position: Offset
    ) {
        scene.sendPointerEvent(eventType = PointerEventType.Move, position = position)
        scene.sendPointerEvent(eventType = PointerEventType.Press, position = position)
        scene.sendPointerEvent(eventType = PointerEventType.Release, position = position)
        settle(scene = scene)
    }

    @OptIn(InternalComposeUiApi::class)
    private fun type(
        scene: ImageComposeScene,
        text: String
    ) {
        text.forEach { char ->
            val typed = AwtKeyEvent(JPanel(), AwtKeyEvent.KEY_TYPED, 0L, 0, AwtKeyEvent.VK_UNDEFINED, char)
            scene.sendKeyEvent(
                event = KeyEvent(
                    key = Key.Unknown,
                    type = KeyEventType.Unknown,
                    codePoint = char.code,
                    nativeEvent = typed
                )
            )
            settle(scene = scene)
        }
    }

    @OptIn(InternalComposeUiApi::class)
    private fun keyDown(
        key: Key
    ): KeyEvent = KeyEvent(key = key, type = KeyEventType.KeyDown)

    private fun settle(
        scene: ImageComposeScene
    ) {
        repeat(times = SETTLE_FRAMES) { scene.render(nanoTime = (frame++) * FRAME_NANOS) }
    }

    companion object {
        private const val BODY = "# Arepas\n\nMix the flour.\n"
        private const val BROKEN_SOURCE = "title: Broken on purpose\nthis line is not a property"
        private const val FRAME_NANOS = 16_000_000L
        private const val HEADER_TEXT =
            "---\ntags: [recipe, venezuelan]\nstatus: Tested\nservings: 4\nfavorite: true\n" +
                "cooked: 2026-10-27\nsource:\n  book: Abuela\n---\n"
        private const val HEIGHT = 520
        private const val NOTE = "Recipes/Arepas.md"
        private const val SETTLE_FRAMES = 4
        private const val TYPED = "!"
        private const val WIDTH = 900
        private val BODY_TEXT = Offset(x = 200f, y = 362f)
        private val BROKEN_SOURCE_END = Offset(x = 600f, y = 206f)
        private val CALENDAR = Offset(x = 406f, y = 264f)
        private val COOKED_TODAY = CalendarDate(year = 2026, month = 10, day = 27)
        private val NEXT_MONTH = Offset(x = 482f, y = 25f)
        private val NOVEMBER_2 = Offset(x = 287f, y = 131f)
        private val OCTOBER_14 = Offset(x = 351f, y = 167f)
        private val TO_PANEL = Offset(x = 752f, y = 105f)
        private val TODAY = Offset(x = 304f, y = 292f)
        private val FAVORITE_BOX = Offset(x = 268f, y = 232f)
        private val HEADER = Offset(x = 150f, y = 105f)
        private val RECIPE_REMOVE = Offset(x = 328f, y = 136f)
        private val SOURCE_TOGGLE = Offset(x = 780f, y = 105f)
        private val STATUS_FIELD_END = Offset(x = 600f, y = 168f)
        private val STATUS_REMOVE = Offset(x = 776f, y = 168f)
    }
}
