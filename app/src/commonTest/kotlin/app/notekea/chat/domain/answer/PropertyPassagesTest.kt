package app.notekea.chat.domain.answer

import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteMetadata
import app.notekea.core.notes.domain.frontmatter.NoteProperty
import app.notekea.core.notes.domain.frontmatter.PropertyValue
import kotlin.test.Test
import kotlin.test.assertEquals

/** The properties of the notes a question names, found whatever the accents. */
class PropertyPassagesTest {
    // Bug hunt: the words were lowercased but kept their accents, so "cafe" never named "Café".
    @Test
    fun `a question names a note whatever the accents of its words`() {
        val cafe = entry(
            path = "Places/Café Łódź.md",
            metadata = NoteMetadata.NONE.copy(
                properties = listOf(
                    NoteProperty(
                        key = "rating",
                        value = PropertyValue.Number(text = "4")
                    )
                )
            )
        )

        assertEquals(
            expected = listOf(
                listOf("Café Łódź — rating: 4"),
                listOf("Café Łódź — rating: 4")
            ),
            actual = listOf("how did I rate the cafe?", "what about lodz?").map { question ->
                PropertyPassages
                    .of(
                        question = question,
                        notes = listOf(cafe),
                        only = null
                    )
                    .map { it.text }
            }
        )
    }
}
