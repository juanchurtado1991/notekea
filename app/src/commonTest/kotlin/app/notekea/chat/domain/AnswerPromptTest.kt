package app.notekea.chat.domain

import app.notekea.chat.domain.prompt.AnswerPrompt
import kotlin.test.Test
import kotlin.test.assertEquals

/** How a reply is read as not found (Auto-Test 3.3-05, 2026-11-13). */
class AnswerPromptTest {
    @Test
    fun `a reply that says what was asked was not found in the notes is not found`() {
        assertEquals(
            // The code review of 2026-11-13: "No hay…", "There is no…" also begin answers, which cite.
            expected = listOf(true, true, false, false, false, true),
            actual = listOf(
                AnswerPrompt.isNotFound(text = "No dentist appointment was found in your notes or tasks."),
                AnswerPrompt.isNotFound(text = "No trips are mentioned in the notes."),
                AnswerPrompt.isNotFound(text = "No, the dough rests for five minutes [1]."),
                AnswerPrompt.isNotFound(text = "No hay azúcar en la lista de ingredientes [1]."),
                AnswerPrompt.isNotFound(text = "There is no sugar in the list of ingredients [2]."),
                AnswerPrompt.isNotFound(text = "No hay nada sobre el dentista en tus notas.")
            )
        )
    }

    // The web's evaluation, 2026-11-16: the notes said to lack a thing, with no word of the list after it, were not
    // read as not found, so the web was never offered; one that lacks it and still answers, citing, is an answer.
    @Test
    fun `a reply that says the notes lack what was asked is not found`() {
        assertEquals(
            expected = listOf(true, true, false),
            actual = listOf(
                AnswerPrompt.isNotFound(text = "The notes do not contain the cost of a metro ride in Lisbon."),
                AnswerPrompt.isNotFound(text = "Las notas no mencionan el precio del metro de Lisboa."),
                AnswerPrompt.isNotFound(text = "The notes do not give a fare, but the trip budget is 940 USD [1].")
            )
        )
    }
}
