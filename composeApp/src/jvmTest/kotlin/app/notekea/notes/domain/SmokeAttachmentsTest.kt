package app.notekea.notes.domain

import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The images and attachments cases of `MVP_SMOKE_TEST.md` (1.8) on the smoke data itself: the note's embeds are found
 * and resolved in the Playground Vault as `smoke.sh prepare` builds it (Personal Notes plus the smoke attachments), and
 * the image files decode as the guide says, so the guide never promises what the data does not hold.
 */
class SmokeAttachmentsTest {
    private val note = RelativePath(value = "Editor/Images and files.md")
    private val embeds = EmbedFinder().embeds(text = File(SMOKE_NOTE).readText())
    private val files = File(PERSONAL_NOTES).walkTopDown()
        .filter {
            it.isFile &&
                it.relativeTo(base = File(PERSONAL_NOTES)).invariantSeparatorsPath.split("/").none { part ->
                    part.startsWith(".")
                }
        }
        .map { RelativePath(value = it.relativeTo(base = File(PERSONAL_NOTES)).invariantSeparatorsPath) }
        .toList() + File(SMOKE_ATTACHMENTS).listFiles().orEmpty().map {
        RelativePath(value = "attachments/" + it.name)
    } +
        note

    @Test
    fun `1_8-01 and 1_8-10 the note's embeds resolve to the files the guide names, the lost photo is missing`() {
        val resolver = AttachmentResolver(
            files = files,
            attachmentFolder = AttachmentFolder(setting = null).folderFor(note = note)
        )

        assertEquals(
            expected = listOf(
                Triple("attachments/Arepas plate.png", null as Int?, true),
                Triple("attachments/Arepas plate.png", 200, true),
                Triple("attachments/Arepas plate.png", null, true),
                Triple("attachments/Weekly menu.pdf", null, true),
                Triple("attachments/broken.png", null, true),
                Triple(null, null, true),
                Triple("attachments/Weekly menu.pdf", null, false),
                Triple("attachments/Arepas plate.png", 80, false)
            ),
            actual = embeds.map { Triple(resolver.resolve(embed = it, note = note)?.value, it.width, it.isOwnLine) }
        )
    }

    @Test
    fun `1_8-13 the note embeds Shopping basics whole and its Dairy section`() {
        val shopping = File(SHOPPING_NOTE).readText()
        val notes = EmbedFinder().embeds(text = File(SMOKE_NOTE).readText(), includeNotes = true).filter { it.isNote }

        assertEquals(
            expected = listOf("Shopping basics", "Shopping basics#Dairy"),
            actual = notes.map { it.target }
        )
        assertEquals(
            expected = "## Dairy\n\n- Fresh cheese\n- Butter",
            actual = EmbeddedNoteText().of(text = shopping, target = notes.last().target)
        )
        assertEquals(
            expected = false,
            actual = EmbeddedNoteText().of(text = shopping, target = notes.first().target).orEmpty().contains("tags:")
        )
    }

    @OptIn(ExperimentalResourceApi::class)
    @Test
    fun `1_8-01 the plate is a picture of 480 x 240, the broken png cannot be drawn and shows as a file`() {
        val plate = File(SMOKE_ATTACHMENTS, "Arepas plate.png").readBytes().decodeToImageBitmap()
        val broken = runCatching { File(SMOKE_ATTACHMENTS, "broken.png").readBytes().decodeToImageBitmap() }.getOrNull()

        assertEquals(
            expected = listOf(480, 240, null),
            actual = listOf(plate.width, plate.height, broken)
        )
        assertEquals(
            expected = listOf(AttachmentKind.IMAGE, AttachmentKind.OTHER),
            actual = listOf("broken.png", "Weekly menu.pdf").map { AttachmentKind.of(name = it) }
        )
    }

    companion object {
        private const val PERSONAL_NOTES = "../smoke-data/vaults/Personal Notes"
        private const val SMOKE_ATTACHMENTS = "../smoke-data/attachments"
        private const val SMOKE_NOTE = "../smoke-data/notes/Images and files.md"
        private const val SHOPPING_NOTE = "../smoke-data/notes/Shopping basics.md"
    }
}
