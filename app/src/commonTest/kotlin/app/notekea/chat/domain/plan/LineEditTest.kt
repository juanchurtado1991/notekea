package app.notekea.chat.domain.plan

import kotlin.test.Test
import kotlin.test.assertEquals

/** `change_line`'s line found by its words and written in place (4.2), the rest of the note as it was. */
class LineEditTest {
    @Test
    fun `a new line keeps the marks of the old one when it writes none of its own`() {
        assertEquals(
            expected = listOf(
                "- 16:00 Interview with Ana",
                "  - [x] call mom",
                "| 3 | Day trip to Cascais |",
                "## Friday"
            ),
            actual = listOf(
                LineEdit.written(
                    old = "- 10:00 Interview with Ana",
                    new = "16:00 Interview with Ana"
                ),
                LineEdit.written(
                    old = "  - [ ] call mom",
                    new = "- [x] call mom"
                ),
                LineEdit.written(
                    old = "| 3 | Day trip to Sintra |",
                    new = " | 3 | Day trip to Cascais | "
                ),
                LineEdit.written(
                    old = "## Thursday",
                    new = "Friday"
                )
            )
        )
    }

    @Test
    fun `the line its words mean is changed, never the frontmatter, and the note keeps its line endings`() {
        val note =
            "---\nslot: 10:00 Interview with Ana\n---\n# Day\r\n- 10:00 Interview with Ana\r\n- 12:00 Lunch\n"

        assertEquals(
            expected = "---\nslot: 10:00 Interview with Ana\n---\n# Day\r\n- 16:00 Interview with Ana\r\n" +
                "- 12:00 Lunch\n",
            actual = LineEdit.changed(
                text = note,
                old = "10:00 interview with Ana",
                new = "16:00 Interview with Ana"
            )
        )
    }

    @Test
    fun `no line, or several alike, is no change`() {
        val note = "# Calls\n- Call mom\n- Call dad\n- Call mom\n"

        assertEquals(
            expected = listOf(null, null, "# Calls\n- Call mom tonight\n- Call dad\n- Call mom\n"),
            actual = listOf(
                LineEdit.changed(
                    text = note,
                    old = "call",
                    new = "x"
                ),
                LineEdit.changed(
                    text = note,
                    old = "Lunch",
                    new = "x"
                ),
                // Identical lines are one: the first is changed, as changing either reads the same.
                LineEdit.changed(
                    text = note,
                    old = "Call mom",
                    new = "Call mom tonight"
                )
            )
        )
    }
}
