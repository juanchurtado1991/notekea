package app.notekea.notes.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class EmbeddedNoteTextTest {
    private val embeddedNoteText = EmbeddedNoteText()
    private val note = """
        ---
        tags: [recipe]
        ---
        # Arepas

        Corn cakes.

        ## Dough

        Corn flour.

        ### Resting

        Ten minutes.

        ## Filling

        Cheese.
    """.trimIndent()

    @Test
    fun `a whole note is shown without its frontmatter, a section down to the next heading of its level`() {
        assertEquals(
            expected = listOf(
                note.substringAfter(delimiter = "---\n").substringAfter(delimiter = "---\n"),
                "## Dough\n\nCorn flour.\n\n### Resting\n\nTen minutes.",
                "### Resting\n\nTen minutes."
            ),
            actual = listOf("Arepas", "Arepas#dough", "Arepas#Resting").map {
                embeddedNoteText.of(text = note, target = it)
            }
        )
    }

    @Test
    fun `a heading or block the note does not have shows nothing`() {
        assertEquals(
            expected = listOf(null, null, null),
            actual = listOf("Arepas#Sauce", "Arepas#^abc123", "Arepas#^").map {
                embeddedNoteText.of(text = note, target = it)
            }
        )
    }

    @Test
    fun `a block is its list item, its paragraph, or the block above a marker on its own line, without the marker`() {
        val text = "Intro line\nSecond line of it ^para\n\n- First\n- Buy flour ^item\n- Last\n\n" +
            "| A | B |\n|---|---|\n| 1 | 2 |\n^table\n"

        assertEquals(
            expected = listOf("Intro line\nSecond line of it", "- Buy flour", "| A | B |\n|---|---|\n| 1 | 2 |"),
            actual = listOf("Note#^para", "Note#^item", "Note#^table").map {
                embeddedNoteText.of(text = text, target = it)
            }
        )
    }
}
