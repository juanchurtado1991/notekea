package app.notekea.notes.domain

import kotlin.test.Test
import kotlin.test.assertEquals

/** The kind of an attachment, the folder new ones go to, how an embed finds its file and how a new one is written. */
class AttachmentRulesTest {
    @Test
    fun `images are the formats Notekea can draw, everything else is a file`() {
        assertEquals(
            expected = listOf(AttachmentKind.IMAGE, AttachmentKind.IMAGE, AttachmentKind.OTHER, AttachmentKind.OTHER),
            actual = listOf("a.PNG", "b.jpeg", "c.svg", "d.pdf").map { AttachmentKind.of(name = it) }
        )
    }

    @Test
    fun `the attachments folder follows Obsidian's setting, attachments by default`() {
        val note = RelativePath(value = "Recipes/Arepas.md")

        assertEquals(
            expected = listOf("attachments", "", "Recipes", "Recipes/images", "Assets/img"),
            actual = listOf(null, "/", "./", "./images", "Assets/img").map {
                AttachmentFolder(setting = it).folderFor(note = note).value
            }
        )
    }

    @Test
    fun `an embed finds its file by path or by name, nearest the note first, ignoring letter case`() {
        val files = listOf(
            "attachments/plate.png",
            "Recipes/plate.png",
            "Other/plate.png",
            "Travel/lisbon.jpg",
            "Recipes/images/dough.png"
        ).map { RelativePath(value = it) }
        val resolver = AttachmentResolver(files = files, attachmentFolder = RelativePath(value = "attachments"))
        val note = RelativePath(value = "Recipes/Arepas.md")
        val embed = { target: String, kind: EmbedKind ->
            Embed(range = 0..0, target = target, kind = kind, isOwnLine = true)
        }

        assertEquals(
            expected = listOf(
                "Recipes/plate.png",
                "attachments/plate.png",
                "Travel/lisbon.jpg",
                "Recipes/images/dough.png",
                null
            ),
            actual = listOf(
                resolver.resolve(embed = embed("Plate.PNG", EmbedKind.WIKI), note = note),
                resolver.resolve(
                    embed = embed("plate.png", EmbedKind.WIKI),
                    note = RelativePath(value = "Journal/Day.md")
                ),
                resolver.resolve(embed = embed("Travel/lisbon.jpg", EmbedKind.WIKI), note = note),
                resolver.resolve(embed = embed("images/dough.png", EmbedKind.MARKDOWN), note = note),
                resolver.resolve(embed = embed("missing.png", EmbedKind.WIKI), note = note)
            ).map { it?.value }
        )
    }

    @Test
    fun `a new embed is written by name when it is unique, by path otherwise, with its width`() {
        val files = listOf("attachments/plate.png", "Recipes/plate.png", "attachments/lisbon.jpg").map {
            RelativePath(value = it)
        }
        val text = EmbedText(files = files)

        assertEquals(
            expected = listOf("![[lisbon.jpg]]", "![[attachments/plate.png|300]]"),
            actual = listOf(
                text.of(path = RelativePath(value = "attachments/lisbon.jpg")),
                text.of(path = RelativePath(value = "attachments/plate.png"), width = 300)
            )
        )
    }
}
