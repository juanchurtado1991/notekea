package app.notekea.chat.domain

import app.notekea.chat.domain.answer.AnswerCitations
import app.notekea.chat.domain.answer.SumsChecked
import app.notekea.chat.domain.plan.TextParts
import kotlin.test.Test
import kotlin.test.assertEquals

/** The chat domain's findings of the code review of 2026-11-13, each as the rule that fixes it. */
class CodeReviewChatTest {
    @Test
    fun `a right sum keeps its thousands marks, a wrong one is done again, and a decimal total reads as one`() {
        assertEquals(
            expected = listOf(
                "4,500 + 300 = 4,800",
                "1.500 + 300 = 1.800",
                "1,250,000 + 5 = 1,250,005",
                "4,500 + 300 = 4800",
                "0.1 + 0.2 = 0.3",
                "7050 (4500 + 300 + 2000 + 250)"
            ),
            actual = listOf(
                "4,500 + 300 = 4,800",
                "1.500 + 300 = 1.800",
                "1,250,000 + 5 = 1,250,005",
                "4,500 + 300 = 5,000",
                "0.1 + 0.2 = 0.4",
                "9250 (4500 + 300 + 2000 + 250)"
            ).map { SumsChecked.corrected(text = it) }
        )
    }

    @Test
    fun `a text pasted as one long paragraph is read whole, in parts, and a long number in brackets is no citation`() {
        val line = "A fact about the harvest. ".repeat(n = 10)
        val text = List(size = 80) { line }.joinToString(separator = "\n")
        val parts = TextParts.of(text = text)

        assertEquals(
            expected = listOf<Any>(true, true, "See [202311081234]."),
            actual = listOf(
                parts.size > 1,
                parts.sumOf { it.length } >= text.length - parts.size * 2 - 80,
                AnswerCitations.of(
                    text = "See [202311081234].",
                    read = emptyList()
                ).text
            )
        )
    }
}
