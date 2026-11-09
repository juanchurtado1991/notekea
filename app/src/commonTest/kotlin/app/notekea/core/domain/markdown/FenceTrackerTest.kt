package app.notekea.core.domain.markdown

import kotlin.test.Test
import kotlin.test.assertEquals

class FenceTrackerTest {
    @Test
    fun `only a bare fence of the same character, at least as long, closes a code block`() {
        assertEquals(
            expected = listOf(true, true, true, true, true, true, false),
            actual = codeLines("````", "```", "~~~~~", "```` not a close", "x", "`````  ", "after")
        )
    }

    @Test
    fun `a line with text after the fence stays inside, and tildes do not close backticks`() {
        assertEquals(
            expected = listOf(true, true, true, true, false),
            actual = codeLines("```bash", "```bash", "~~~", "```", "after")
        )
    }

    @Test
    fun `a closing fence indented four spaces is code, as in CommonMark, and a line break after it does not count`() {
        assertEquals(
            expected = listOf(true, true, true, false),
            actual = codeLines("```", "    ```", "```\r", "after")
        )
    }

    @Test
    fun `text outside a block is not code`() {
        assertEquals(expected = listOf(false, false), actual = codeLines("plain", "`` inline ``"))
    }

    @Test
    fun `a line is read in place from a longer text`() {
        val text = "```\ncode\n```\nafter"
        val tracker = FenceTracker()

        assertEquals(
            expected = listOf(true, true, true, false),
            actual = listOf(0..3, 4..8, 9..12, 13..18).map {
                tracker.read(text = text, start = it.first, end = it.last)
            }
        )
    }

    private fun codeLines(
        vararg lines: String
    ): List<Boolean> {
        val tracker = FenceTracker()
        return lines.map { tracker.read(text = it) }
    }
}
