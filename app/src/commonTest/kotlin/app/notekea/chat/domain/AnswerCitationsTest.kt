package app.notekea.chat.domain

import app.notekea.core.notes.domain.RelativePath
import kotlin.test.Test
import kotlin.test.assertEquals

class AnswerCitationsTest {
    private val read = listOf("Ingredients", "Steps", "Serving").mapIndexed { index, heading ->
        AnswerSource(
            number = index + 1,
            note = RelativePath(value = "Recipes/Arepas.md"),
            headingPath = listOf(heading),
            range = index * 10 until index * 10 + 5,
            text = heading
        )
    }

    @Test
    fun `the cited excerpts are numbered again in the order they appear, and only they are sources`() {
        val answer = AnswerCitations.of(
            text = "Rest it five minutes [2]. Mix flour and water [3][2].",
            read = read
        )

        assertEquals(
            expected = "Rest it five minutes [1]. Mix flour and water [2] [1]." to listOf("Steps", "Serving"),
            actual = answer.text to answer.sources.map { it.text }
        )

        assertEquals(
            expected = listOf(1, 2),
            actual = answer.sources.map { it.number }
        )
    }

    @Test
    fun `a citation of an excerpt that was not given is dropped, with the space before it`() {
        val answer = AnswerCitations.of(
            text = "Mix flour [7], then water [1].",
            read = read
        )

        assertEquals(
            expected = "Mix flour, then water [1]." to 1,
            actual = answer.text to answer.sources.size
        )
    }
}
