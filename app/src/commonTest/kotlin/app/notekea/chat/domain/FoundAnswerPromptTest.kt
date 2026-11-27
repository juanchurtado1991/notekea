package app.notekea.chat.domain

import app.notekea.chat.domain.prompt.FoundAnswerPrompt
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The answer's instructions carry no example figures: the 4B and the 9B wrote "(17 of 24 read, 7 left)", the
 * example the prompt gave, into their answers as if it were a source (the web's evaluation, 2026-11-16). A page is
 * cited by its link, not "page 1", and is not called a note (the third run: "las notas no contienen…" of pages read).
 */
class FoundAnswerPromptTest {
    @Test
    fun `the answer is asked for with no example figures, and a page cited by its link`() {
        val asked = FoundAnswerPrompt
            .answer(
                task = "what is the population of Lisbon?",
                found = listOf("fetch https://en.wikipedia.org/wiki/Lisbon: 545,000 within the city limits")
            ).joinToString(separator = "\n") { it.text }

        assertEquals(
            expected = listOf(false, true, true, true),
            actual = listOf(
                "17 of 24" in asked,
                "Markdown link to its whole address" in asked,
                "never \"page 1\"" in asked,
                "A page read is a page, not a note." in asked
            )
        )
    }

    // The role-play of 2026-11-16: the 9B's answer began "Nota: la conclusión anterior incluye…" and listed its
    // steps; the answer is asked for alone.
    @Test
    fun `the answer is asked for alone, with no note about the conclusion and no steps`() {
        val asked = FoundAnswerPrompt
            .answer(
                task = "qué piezas necesito para un riego por goteo",
                found = listOf("fetch https://kafinternacional.com: cintas de goteo, goteros, estacas"),
                conclusion = "Cintas, goteros, conectores y válvulas."
            ).joinToString(separator = "\n") { it.text }

        assertEquals(
            expected = listOf(true, true),
            actual = listOf(
                "never a note about your conclusion, nor the steps" in asked,
                "leaves the rest out without saying so" in asked
            )
        )
    }
}
