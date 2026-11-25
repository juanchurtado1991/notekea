package app.notekea.chat.domain.plan

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The figures a written note may hold: those its sources write and those worked out from them. Dev, 2026-11-25: a
 * right total was taken for a made-up one, and the note written again without it.
 */
class BackedFiguresTest {
    private val sources = listOf(
        "Rent: 1,250.00, due 12.03.2026",
        "Lamp: 45.50\nDesk: 310\nChair: 120"
    )

    @Test
    fun `what a table's column or a list adds up to is backed, and any other total is not`() {
        val table =
            """
            | Item | Price |
            |---|---|
            | Lamp | 45.50 |
            | Desk | 310 |
            | Chair | 120 |
            """.trimIndent()

        val list = "- Lamp: 45.50\n- Desk: 310"

        assertEquals(
            expected = listOf(emptyList(), listOf("476.50"), emptyList(), listOf("365")),
            actual = listOf(
                missingIn(text = "$table\n| **Total** | **475.50** |"),
                missingIn(text = "$table\n\nTotal: 476.50"),
                missingIn(text = "$list\n\nTotal: 355.50"),
                missingIn(text = "$list\n\nTotal: 365")
            )
        )
    }

    @Test
    fun `the sum and the difference of two figures of a line are backed on that line only`() {
        assertEquals(
            expected = listOf(emptyList(), emptyList(), listOf("190")),
            actual = listOf(
                missingIn(text = "Desk 310 and chair 120: 430 together."),
                missingIn(text = "The desk (310) costs 190 more than the chair (120)."),
                missingIn(text = "Desk 310, chair 120.\nThat is 190 more.")
            )
        )
    }

    @Test
    fun `a figure is one value however it is written, and one that is no value is backed as it is written`() {
        assertEquals(
            expected = listOf(emptyList(), listOf("1251"), emptyList(), listOf("13.03.2026")),
            actual = listOf(
                missingIn(text = "The rent is 1250, or 1.250,00."),
                missingIn(text = "The rent is 1251."),
                missingIn(text = "Due 12.03.2026."),
                missingIn(text = "Due 13.03.2026.")
            )
        )
    }

    private fun missingIn(text: String): List<String> = BackedFigures.missingIn(
        text = text,
        sources = sources
    )
}
