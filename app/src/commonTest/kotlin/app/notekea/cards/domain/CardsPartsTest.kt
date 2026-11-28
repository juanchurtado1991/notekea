package app.notekea.cards.domain

import app.notekea.core.notes.domain.NoteSections
import app.notekea.core.notes.domain.OutlineFinder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** A long note's cards are made part by part: its sections in order, as many as fit, a long one cut at paragraphs. */
class CardsPartsTest {
    private val sections = NoteSections(outlineFinder = OutlineFinder())

    private fun partsOf(
        text: String,
        max: Int
    ): List<String> = CardsParts.of(
        text = text,
        sections = sections.of(body = text),
        max = max
    )

    @Test
    fun `a note that fits is one part, as it is`() {
        val text = "# Short\n\nOne line."

        assertEquals(
            expected = listOf(text),
            actual = partsOf(
                text = text,
                max = 100
            )
        )
    }

    @Test
    fun `sections are packed in order while they fit, and a long one is cut at its paragraphs`() {
        val first = "## One\n\n" + "a".repeat(n = 30)
        val second = "## Two\n\n" + "b".repeat(n = 30)
        val long = "## Three\n\n" + "c".repeat(n = 50) + "\n\n" + "d".repeat(n = 50)

        val parts = partsOf(
            text = listOf(first, second, long).joinToString(separator = "\n\n"),
            max = 80
        )

        assertEquals(
            expected = listOf("## One", "## Three", "ddddd"),
            actual = parts.map {
                it.take(
                    n = it.indexOfFirst { char ->
                        char == '\n'
                    }.takeIf { i ->
                        i > 0
                    } ?: 5
                )
            }
        )

        assertTrue(
            actual = parts.all {
                it.length <= 80
            }
        )

        assertTrue(actual = "## Two" in parts.first())
    }
}
