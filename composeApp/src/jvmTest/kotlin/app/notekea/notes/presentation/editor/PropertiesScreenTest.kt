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
import app.notekea.notes.domain.FrontmatterReader
import app.notekea.notes.domain.PropertyValue
import app.notekea.notes.domain.RelativePath
import javax.swing.JPanel
import kotlin.test.Test
import kotlin.test.assertEquals
import java.awt.event.KeyEvent as AwtKeyEvent

/**
 * The properties under a note's title (`ROADMAP.md` 1.14), rendered headless: the header folds them, a checkbox, a
 * chip's ✕ and a row's ✕ edit at once, typed text is committed with Enter, and Show source swaps the form.
 */
class PropertiesScreenTest {
    private var frame = 0L

    @Test
    fun `the form folds, edits a checkbox, a list, a text and removes a property, and shows its source`() {
        val intents = mutableListOf<EditorIntent>()
        UiThread.run {
            scene(intents = intents).use { scene ->
                settle(scene = scene)
                listOf(FAVORITE_BOX, RECIPE_REMOVE, STATUS_REMOVE, STATUS_FIELD_END).forEach { click(scene, it) }
                type(scene = scene, text = TYPED)
                scene.sendKeyEvent(event = keyDown(key = Key.Enter))
                settle(scene = scene)
                listOf(SOURCE_TOGGLE, HEADER).forEach { click(scene, it) }
            }
        }

        assertEquals(
            expected = listOf(
                PropertyIntent.OnValueChange(key = "favorite", value = PropertyValue.Checkbox(isChecked = false)),
                PropertyIntent.OnValueChange(key = "tags", value = PropertyValue.Items(items = listOf("venezuelan"))),
                PropertyIntent.OnRemove(key = "status"),
                PropertyIntent.OnValueChange(key = "status", value = PropertyValue.Text(text = "Tested$TYPED")),
                PropertyIntent.OnSourceToggle,
                PropertyIntent.OnFoldToggle
            ),
            actual = intents.filterIsInstance<PropertyIntent>()
        )
    }

    private fun scene(
        intents: MutableList<EditorIntent>
    ) = ImageComposeScene(width = WIDTH, height = HEIGHT, density = Density(density = 1f)) {
        NotekeaTheme(darkTheme = false) {
            EditorContent(
                state = EditorState(
                    notePath = RelativePath(value = NOTE),
                    properties = FrontmatterReader().read(header = HEADER_TEXT)
                ),
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
        private const val FRAME_NANOS = 16_000_000L
        private const val HEADER_TEXT =
            "---\ntags: [recipe, venezuelan]\nstatus: Tested\nservings: 4\nfavorite: true\n" +
                "cooked: 2026-10-27\nsource:\n  book: Abuela\n---\n"
        private const val HEIGHT = 520
        private const val NOTE = "Recipes/Arepas.md"
        private const val SETTLE_FRAMES = 4
        private const val TYPED = "!"
        private const val WIDTH = 900
        private val FAVORITE_BOX = Offset(x = 273f, y = 213f)
        private val HEADER = Offset(x = 150f, y = 101f)
        private val RECIPE_REMOVE = Offset(x = 334f, y = 130f)
        private val SOURCE_TOGGLE = Offset(x = 784f, y = 101f)
        private val STATUS_FIELD_END = Offset(x = 600f, y = 157f)
        private val STATUS_REMOVE = Offset(x = 784f, y = 157f)
    }
}
