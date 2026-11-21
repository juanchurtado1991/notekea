package app.notekea.core.notes.domain

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

    @Test
    fun `a shorter fence inside a longer one does not end the code`() {
        val lines = sequenceOf("````", "```", "#inside", "````", "#outside")

        assertEquals(
            expected = listOf("outside"),
            actual = TextTagFinder().tags(lines = lines)
        )
    }

    // Bug hunt S4: parentheses in a link's target, or an address's anchor, ended the target and made a tag.
    @Test
    fun `an anchor after parentheses in a link, in an address or after a quote is no tag`() {
        val lines = sequenceOf(
            "Read [Dune](https://en.wikipedia.org/wiki/Dune_(novel)#Plot) #book",
            "Or https://a.b/c_(d)#top and <a href=\"x\"#anchor> nor key=#value",
            "A (note)#plain stays text"
        )

        assertEquals(
            expected = listOf("book"),
            actual = TextTagFinder().tags(lines = lines)
        )
    }
}
