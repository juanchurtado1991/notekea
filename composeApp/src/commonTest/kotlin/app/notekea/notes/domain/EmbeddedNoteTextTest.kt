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
    fun `a heading the note does not have and a block reference stay links`() {
        assertEquals(
            expected = listOf(null, null),
            actual = listOf("Arepas#Sauce", "Arepas#^abc123").map { embeddedNoteText.of(text = note, target = it) }
        )
    }
}
