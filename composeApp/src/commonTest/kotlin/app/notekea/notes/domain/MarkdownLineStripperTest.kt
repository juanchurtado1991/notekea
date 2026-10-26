package app.notekea.notes.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class MarkdownLineStripperTest {
    private val stripper = MarkdownLineStripper()

    @Test
    fun `block markers at the start of a line are removed`() {
        assertEquals(
            expected = listOf("Tomato plan", "Add tomatoes", "Buy flour", "Done", "Quoted", "Second step"),
            actual = listOf(
                "# Tomato plan",
                "- Add tomatoes",
                "  * [ ] Buy flour",
                "- [x] Done",
                "> Quoted",
                "2. Second step"
            )
                .map { stripper.strip(line = it, ranges = emptyList()).text }
        )
    }

    @Test
    fun `links keep only what a reader sees, an alias instead of its target`() {
        assertEquals(
            expected = "Notes in Tomato plan, see the coffee and the recipe or a photo.",
            actual = stripper.strip(
                line =
                "Notes in [[Tomato plan]], see [[Café con leche|the coffee]] and [the recipe](Recipes/Arepas.md) " +
                    "or ![a photo](photo.png).",
                ranges = emptyList()
            ).text
        )
    }

    @Test
    fun `emphasis and code marks go, while snake_case, tags and code content stay`() {
        assertEquals(
            expected = "2 cups of pre-cooked corn, so tasty, done, snake_case #recipe and **raw** code",
            actual = stripper.strip(
                line = "2 cups of **pre-cooked** corn, *so* _tasty_, ~~done~~, snake_case #recipe and `**raw** code`",
                ranges = emptyList()
            ).text
        )
    }

    @Test
    fun `highlights move with the text they cover and vanish with removed syntax`() {
        val line = "- Planted **tomato** seedlings; notes in [[Tomato plan]]."
        val tomato = line.indexOf(string = "tomato")
        val linked = line.indexOf(string = "Tomato plan")
        val brackets = line.indexOf(string = "[[")

        val plain = stripper.strip(
            line = line,
            ranges = listOf(tomato..tomato + 5, linked..linked + 5, brackets..brackets + 1)
        )

        assertEquals(
            expected = "Planted tomato seedlings; notes in Tomato plan." to listOf("tomato", "Tomato"),
            actual = plain.text to plain.ranges.map { plain.text.substring(range = it) }
        )
    }
}
