package app.notekea.notes.domain

import app.notekea.notes.domain.NoteFixtures.entry
import kotlin.test.Test
import kotlin.test.assertEquals

/** Which notes are templates and how the picker names them (`ROADMAP.md` 1.12). */
class TemplatesTest {
    private val templates = Templates()
    private val tree = listOf(
        "Templates/Meeting notes.md",
        "Templates/Work/Weekly review.md",
        "Templates/Day 10.md",
        "Templates/Day 9.md",
        "Ideas.md"
    ).fold(initial = VaultTree.EMPTY) { tree, path ->
        tree.withNote(note = entry(path = path))
    }

    @Test
    fun `the notes of the templates folder and its subfolders are the templates, in natural order`() {
        val listing = templates.listing(tree = tree, settings = TemplateSettings.DEFAULT) as TemplateListing.Found

        assertEquals(
            expected = listOf("Day 9", "Day 10", "Meeting notes", "Work/Weekly review"),
            actual = listing.templates.map { templates.nameOf(template = it, settings = TemplateSettings.DEFAULT) }
        )
    }

    @Test
    fun `a missing folder, an empty one and the vault root as the folder are told apart`() {
        val elsewhere = TemplateSettings.DEFAULT.copy(folder = "Snippets")
        val root = TemplateSettings.DEFAULT.copy(folder = "")

        assertEquals(
            expected = listOf(TemplateListing.NoFolder, TemplateListing.Found(templates = emptyList()), 5),
            actual = listOf(
                templates.listing(tree = tree, settings = elsewhere),
                templates.listing(
                    tree = tree.withFolder(path = RelativePath(value = "Snippets")),
                    settings = elsewhere
                ),
                (templates.listing(tree = tree, settings = root) as TemplateListing.Found).templates.size
            )
        )
    }
}
