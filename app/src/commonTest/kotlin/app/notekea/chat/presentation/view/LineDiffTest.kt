package app.notekea.chat.presentation.view

import kotlin.test.Test
import kotlin.test.assertEquals

/** The words the card marks on a changed line (4.2, design B): only those that differ, in each line. */
class LineDiffTest {
    private fun marked(
        old: String,
        new: String
    ): List<String> {
        val change = LineDiff.of(
            old = old,
            new = new
        )

        return listOf(old.substring(range = change.old), new.substring(range = change.new))
    }

    @Test
    fun `the words that differ are marked, and a word only added marks nothing in the old line`() {
        assertEquals(
            expected = listOf(
                listOf("10:00", "16:00"),
                listOf("", "Cascais and"),
                listOf("", "(call)"),
                listOf("old line", "new words")
            ),
            actual = listOf(
                marked(
                    old = "10:00 Interview with Ana",
                    new = "16:00 Interview with Ana"
                ),
                marked(
                    old = "| 3 | Sintra |",
                    new = "| 3 | Cascais and Sintra |"
                ),
                marked(
                    old = "Interview with Ana",
                    new = "Interview with Ana (call)"
                ),
                marked(
                    old = "old line",
                    new = "new words"
                )
            )
        )
    }
}
