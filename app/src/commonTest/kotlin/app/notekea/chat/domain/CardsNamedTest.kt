package app.notekea.chat.domain

import app.notekea.chat.domain.plan.ActionContext
import app.notekea.chat.domain.plan.ActionField
import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.CardsNamed
import app.notekea.chat.domain.plan.PlannedStep
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * "Crea tarjetas de estudio de WordPress" (the developer's smoke of 2026-11-13): the model planned nothing it could
 * do and agent mode wrote a note of its own; the request is now a card step for each note it is about.
 */
class CardsNamedTest {
    private val context = ActionContext(
        today = "2026-11-13",
        notes = listOf(
            "Recipes/Arepas",
            "Web sources/Documentation/Hosting WordPress",
            "Web sources/Documentation/First steps with WordPress (Block Editor)",
            "Web sources/WordPress",
            "Books/Sapiens"
        )
    )

    private fun cards(note: String) = PlannedStep(
        kind = ActionKind.MAKE_CARDS,
        fields = mapOf(ActionField.NOTE to note)
    )

    private fun shaped(
        request: String,
        steps: List<PlannedStep> = emptyList()
    ) = CardsNamed.shaped(
        steps = steps,
        request = request,
        context = context
    )

    @Test
    fun `cards of a subject are cards of the notes named after it, and a note the model named is kept`() {
        val question = listOf(PlannedStep(kind = ActionKind.ANSWER))

        assertEquals(
            expected = listOf(
                listOf(
                    cards(note = "Web sources/WordPress"),
                    cards(note = "Web sources/Documentation/Hosting WordPress"),
                    cards(note = "Web sources/Documentation/First steps with WordPress (Block Editor)")
                ),
                listOf(cards(note = "Sapiens")),
                question,
                question
            ),
            actual = listOf(
                shaped(
                    request = "crea tarjetas de estudio de wordpress",
                    steps = question
                ),
                shaped(
                    request = "hazme tarjetas de estudio de Sapiens",
                    steps = listOf(cards(note = "Sapiens"))
                ),
                shaped(
                    request = "make study cards about quantum physics",
                    steps = question
                ),
                shaped(
                    request = "what is in my notes about wordpress?",
                    steps = question
                )
            )
        )
    }
}
