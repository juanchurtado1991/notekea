package app.notekea.notes.presentation.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import app.notekea.core.presentation.UiThread
import app.notekea.core.presentation.clickText
import app.notekea.core.presentation.texts
import app.notekea.core.presentation.theme.NotekeaTheme
import app.notekea.notes.domain.AttachmentKind
import app.notekea.notes.domain.Embed
import app.notekea.notes.domain.EmbedKind
import app.notekea.notes.domain.EmbedProblem
import app.notekea.notes.domain.LinkKind
import app.notekea.notes.domain.RelativePath
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Embedded notes inside each other and the notes that cannot be shown (`ROADMAP.md` 1.15), rendered headless: a nested
 * note's frame and a picture show inside their parent's (a picture there crashed the layout while the frame measured
 * its intrinsic height), each notice says why in plain words, Create note asks for the missing note as a link to
 * it does, and Open note opens the note.
 */
class EmbedNoticeScreenTest {
    @Test
    fun `a nested note shows inside its frame, notices explain themselves, and their buttons create or open notes`() {
        val intents = mutableListOf<EditorIntent>()
        var texts = emptyList<String>()
        UiThread.run {
            ImageComposeScene(width = WIDTH, height = HEIGHT, density = Density(density = 1f)) {
                NotekeaTheme(darkTheme = false) {
                    Column {
                        listOf(nested, missing, noHeading, circular).forEach {
                            AttachmentBlock(view = it, onIntent = { intent -> intents += intent })
                        }
                    }
                }
            }.use { scene ->
                scene.render(nanoTime = 0L)
                texts = scene.texts()
                scene.clickText(text = CREATE) { scene.render(nanoTime = 0L) }
                // The frames' titles say Open note too; the missing heading's button is the last one.
                scene.clickText(text = OPEN_NOTE, occurrence = OPEN_NOTE_BUTTON) { scene.render(nanoTime = 0L) }
            }
        }

        assertEquals(
            expected = listOf(
                "Pantry",
                "Shelf",
                "This note does not exist yet.",
                "Arepas has no heading “Dessert”.",
                "Circular embed: this note is already shown above, so it stops here."
            ),
            actual = texts.filter { it in EXPECTED_TEXTS }.distinct()
        )
        assertEquals(
            expected = listOf<EditorIntent>(
                EditorIntent.OnLinkClick(link = ReadLink.Note(target = "Weekend menu", kind = LinkKind.WIKI)),
                EditorIntent.OnLinkClick(link = ReadLink.Note(target = "Recipes/Arepas", kind = LinkKind.WIKI))
            ),
            actual = intents
        )
    }

    private val nested = note(
        target = "Pantry",
        path = "Pantry.md",
        text = "Corn flour\n![[plate.png]]\n![[Shelf]]",
        nested = listOf(
            AttachmentView(
                embed = Embed(range = 11..24, target = "plate.png", kind = EmbedKind.WIKI, isOwnLine = true),
                kind = AttachmentKind.IMAGE,
                path = RelativePath(value = "attachments/plate.png"),
                image = ImageBitmap(width = PLATE_WIDTH, height = PLATE_HEIGHT)
            ),
            note(target = "Shelf", path = "Shelf.md", text = "Top shelf", range = 26..35)
        )
    )
    private val missing = note(target = "Weekend menu", path = null, problem = EmbedProblem.MISSING_NOTE)
    private val noHeading =
        note(target = "Arepas#Dessert", path = "Recipes/Arepas.md", problem = EmbedProblem.MISSING_SECTION)
    private val circular = note(target = "Pantry", path = "Pantry.md", problem = EmbedProblem.CIRCULAR)

    private fun note(
        target: String,
        path: String?,
        text: String? = null,
        problem: EmbedProblem? = null,
        nested: List<AttachmentView> = emptyList(),
        range: IntRange = 0..0
    ) = AttachmentView(
        embed = Embed(range = range, target = target, kind = EmbedKind.WIKI, isOwnLine = true, isNote = true),
        kind = AttachmentKind.NOTE,
        path = path?.let { RelativePath(value = it) },
        noteText = text,
        nested = nested,
        problem = problem
    )

    companion object {
        private const val CREATE = "Create note"
        private const val HEIGHT = 1000
        private const val OPEN_NOTE = "Open note"
        private const val PLATE_HEIGHT = 20
        private const val PLATE_WIDTH = 40
        private const val OPEN_NOTE_BUTTON = 2
        private const val WIDTH = 800
        private val EXPECTED_TEXTS = setOf(
            "Pantry",
            "Shelf",
            "This note does not exist yet.",
            "Arepas has no heading “Dessert”.",
            "Circular embed: this note is already shown above, so it stops here."
        )
    }
}
