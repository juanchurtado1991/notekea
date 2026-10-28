package app.notekea.notes.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class TextTagFinderTest {
    @Test
    fun `tags of the text are found once each, outside code, links and headings`() {
        val text = """
            # Weekend arepas #recipe
            Fry them #recipe #weekend-plan and #recipe/drinks, not #2026 nor a#b.
            See [[Arepas#Ingredients]], [[#Top]], [the top](#top) and `#not-a-tag`.
            ```
            #inside-code
            ```
            ~~~
            #inside-tildes
            ~~~
            Served with #café.
        """.trimIndent()

        assertEquals(
            expected = listOf("recipe", "weekend-plan", "recipe/drinks", "café"),
            actual = TextTagFinder().tags(lines = text.lineSequence())
        )
    }
}
