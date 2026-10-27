package app.notekea.notes.presentation.editor

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.preview.PreviewPlatformConventions
import app.notekea.core.presentation.theme.NotekeaTheme
import app.notekea.notes.domain.AttachmentKind
import app.notekea.notes.domain.EmbedFinder
import app.notekea.notes.domain.RelativePath
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A note's attachments on screen (`ROADMAP.md` 1.8), rendered headless on the AWT event thread: in Edit mode each
 * file shows under its line and its chip opens it; in Preview a missing file offers Locate…; Mod+V and the toolbar's
 * More › Image or file ask the editor to attach. Positions are those of a 1200 × 900 px window at 1.5 density.
 */
class EditorAttachmentsScreenTest {
    @Test
    fun `in edit mode the file chip under its line opens the file, mod+v pastes and more picks a file`() {
        val intents = render(isReading = false) { scene ->
            click(scene = scene, position = EDIT_CHIP)
            click(scene = scene, position = TEXT_LINE)
            scene.sendKeyEvent(event = ctrlV())
            click(scene = scene, position = MORE_BUTTON)
            click(scene = scene, position = ATTACH_ITEM)
        }

        assertEquals(
            expected = listOf(
                EditorIntent.OnAttachmentOpen(path = MENU),
                EditorIntent.OnPaste,
                EditorIntent.OnAttachmentPick
            ),
            actual = intents.filterNot { it == EditorIntent.OnFocusLost }
        )
    }

    @Test
    fun `in preview the chip opens the file and a missing one offers locate`() {
        val intents = render(isReading = true) { scene ->
            click(scene = scene, position = READ_CHIP)
            click(scene = scene, position = READ_LOCATE)
        }

        assertEquals(
            expected = listOf(
                EditorIntent.OnAttachmentOpen(path = MENU),
                EditorIntent.OnAttachmentLocate(embed = EmbedFinder().embeds(text = TEXT)[2])
            ),
            actual = intents
        )
    }

    private fun render(
        isReading: Boolean,
        actions: (ImageComposeScene) -> Unit
    ): List<EditorIntent> {
        val intents = mutableListOf<EditorIntent>()
        UiThread.run {
            ImageComposeScene(width = WINDOW_WIDTH, height = WINDOW_HEIGHT, density = Density(density = DENSITY)) {
                NotekeaTheme(darkTheme = false) {
                    EditorContent(
                        state = EditorState(notePath = NOTE, isReading = isReading, attachments = views()),
                        buffer = TextFieldState(initialText = TEXT),
                        platformConventions = PreviewPlatformConventions.linux,
                        onIntent = { intents += it }
                    )
                }
            }.use { scene ->
                settle(scene = scene)
                actions(scene)
            }
        }
        return intents
    }

    private fun views(): List<AttachmentView> {
        val embeds = EmbedFinder().embeds(text = TEXT)
        val picture = ImageBitmap(width = IMAGE_WIDTH, height = IMAGE_HEIGHT).also {
            Canvas(image = it).drawRect(rect = Rect(left = 0f, top = 0f, right = 600f, bottom = 300f), paint = Paint())
        }
        return listOf(
            AttachmentView(
                embed = embeds[0],
                kind = AttachmentKind.IMAGE,
                path = RelativePath(value = PLATE),
                image = picture
            ),
            AttachmentView(embed = embeds[1], kind = AttachmentKind.OTHER, path = MENU),
            AttachmentView(embed = embeds[2], kind = AttachmentKind.IMAGE, path = null)
        )
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

    private var frame = 0L

    private fun settle(
        scene: ImageComposeScene
    ) {
        repeat(times = SETTLE_FRAMES) {
            frame += FRAME_NANOS
            scene.render(nanoTime = frame)
            Thread.sleep(PARSE_PAUSE_MILLIS)
        }
    }

    @OptIn(InternalComposeUiApi::class)
    private fun ctrlV(): KeyEvent = KeyEvent(key = Key.V, type = KeyEventType.KeyDown, isCtrlPressed = true)

    companion object {
        private const val DENSITY = 1.5f
        private const val FRAME_NANOS = 160_000_000L
        private const val IMAGE_HEIGHT = 300
        private const val IMAGE_WIDTH = 600
        private const val PARSE_PAUSE_MILLIS = 30L
        private const val PLATE = "attachments/plate.png"
        private const val SETTLE_FRAMES = 6
        private const val TEXT =
            "# Arepas\n\nThe plate:\n![[plate.png|300]]\nThe menu:\n![[menu.pdf]]\n![[gone.png]]\nEnd.\n"
        private const val WINDOW_HEIGHT = 900
        private const val WINDOW_WIDTH = 1200
        private val ATTACH_ITEM = Offset(x = 842f, y = 513f)
        private val MORE_BUTTON = Offset(x = 1035f, y = 186f)
        private val EDIT_CHIP = Offset(x = 255f, y = 877f)
        private val MENU = RelativePath(value = "Recipes/menu.pdf")
        private val NOTE = RelativePath(value = "Recipes/Arepas.md")
        private val READ_CHIP = Offset(x = 255f, y = 668f)
        private val READ_LOCATE = Offset(x = 1023f, y = 767f)
        private val TEXT_LINE = Offset(x = 300f, y = 344f)
    }
}
