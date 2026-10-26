package app.notekea.notes.presentation.editor

import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.theme.NotekeaTheme
import app.notekea.notes.domain.AttachmentKind
import app.notekea.notes.domain.Embed
import app.notekea.notes.domain.EmbedKind
import app.notekea.notes.domain.LinkKind
import app.notekea.notes.domain.RelativePath
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 1.8 smoke request: a note embedded with `![[Note]]` shows its text in place, as in Obsidian; a click on its title
 * opens the note.
 */
class EmbeddedNoteScreenTest {
    @Test
    fun `clicking an embedded note's title opens that note`() {
        val intents = mutableListOf<EditorIntent>()
        UiThread.run {
            ImageComposeScene(width = WINDOW_WIDTH, height = WINDOW_HEIGHT, density = Density(density = DENSITY)) {
                NotekeaTheme(darkTheme = false) {
                    AttachmentBlock(
                        view = AttachmentView(
                            embed = Embed(range = 0..0, target = TARGET, kind = EmbedKind.WIKI, isOwnLine = true),
                            kind = AttachmentKind.NOTE,
                            path = PATH,
                            noteText = TEXT
                        ),
                        onIntent = { intents += it }
                    )
                }
            }.use { scene ->
                scene.render(nanoTime = 0L)
                scene.sendPointerEvent(eventType = PointerEventType.Press, position = TITLE)
                scene.sendPointerEvent(eventType = PointerEventType.Release, position = TITLE)
                scene.render(nanoTime = FRAME_NANOS)
            }
        }

        assertEquals(
            expected = listOf<EditorIntent>(
                EditorIntent.OnLinkClick(link = ReadLink.Note(target = "Editor/Shopping basics", kind = LinkKind.WIKI))
            ),
            actual = intents
        )
    }

    companion object {
        private const val DENSITY = 1f
        private const val FRAME_NANOS = 16_000_000L
        private const val TARGET = "Shopping basics#Dairy"
        private const val TEXT = "## Dairy\n\n- Fresh cheese"
        private const val WINDOW_HEIGHT = 300
        private const val WINDOW_WIDTH = 600
        private val PATH = RelativePath(value = "Editor/Shopping basics.md")
        private val TITLE = Offset(x = 60f, y = 21f)
    }
}
