package app.notekea.chat.eval

import kotlin.test.Test
import kotlin.test.assertEquals

class AnswerGradeTest {
    private val water = AnswerQuestion(
        vault = "Sample Vault",
        question = "¿Cuánta agua lleva la masa?",
        notes = listOf("Recipes/Arepas.md"),
        facts = listOf(listOf("2½", "dos tazas y media", "2.5"))
    )

    private val trap = water.copy(
        question = "When is the dentist?",
        facts = emptyList()
    )

    @Test
    fun `a right answer holds every fact in the question's language, and a trap is right when declined`() {
        assertEquals(
            expected = listOf(true, false, false, true, false),
            actual = listOf(
                AnswerGrade.isRight(water, emptyList(), "La masa lleva dos tazas y media de agua [1]."),
                AnswerGrade.isRight(water, emptyList(), "The dough takes 2.5 cups of water [1]."),
                AnswerGrade.isRight(water, emptyList(), null),
                AnswerGrade.isRight(trap, emptyList(), null),
                AnswerGrade.isRight(trap, emptyList(), "On Monday.")
            )
        )
    }

    @Test
    fun `facts are found without case or accents, and pasted Markdown or a long copied run fails`() {
        val excerpt = (1..30).joinToString(separator = " ") { "word$it" }

        assertEquals(
            expected = listOf(true, true, true, false),
            actual = listOf(
                AnswerGrade.hasFacts(water, "DOS TAZAS Y MEDIA"),
                AnswerGrade.isPasted("## Ingredients\n- two cups", emptyList()),
                AnswerGrade.isPasted("So $excerpt.", listOf(excerpt)),
                AnswerGrade.isPasted("Two cups of flour and water.", listOf(excerpt))
            )
        )
    }
}
