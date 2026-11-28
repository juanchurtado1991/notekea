package app.notekea.chat.domain

import app.notekea.chat.domain.language.inLanguage
import app.notekea.chat.domain.prompt.AnswerPrompt
import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.ChatRole
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The later rounds of the chat probe of 2026-11-11, each as the rule that fixes it: a not found kept in the chat's
 * language, and the not found replies told from answers.
 */
class ChatProbeRoundsTest {
    @Test
    fun `not found is kept in any language, and its paraphrase is not found too`() {
        val messages = listOf(
            ChatMessage(
                role = ChatRole.USER,
                text = "Q"
            )
        )

        assertEquals(
            expected = listOf(true, true, true, true, true, false, false),
            actual = listOf(
                messages
                    .inLanguage(
                        language = "Spanish",
                        kept = AnswerPrompt.NOT_FOUND
                    )
                    .last()
                    .text
                    .endsWith("When your reply is NOT_FOUND, write NOT_FOUND as it is."),
                AnswerPrompt.isNotFound(text = "No se menciona en los extractos cuántos libros terminaste."),
                AnswerPrompt.isNotFound(text = "No se encontró información en los extractos sobre las recetas."),
                AnswerPrompt.isNotFound(text = "The excerpts do not mention the owner."),
                AnswerPrompt.isNotFound(
                    text = "No se puede determinar cuántos libros terminaste porque los fragmentos no lo indican [1]."
                ),
                AnswerPrompt.isNotFound(text = "Ningún proyecto está atrasado según los fragmentos [1]."),
                AnswerPrompt.isNotFound(text = "You wrote that the arepas need 2½ cups of water [1].")
            )
        )
    }

    @Test
    fun `a long reply that says the list does not tell is not found`() {
        assertEquals(
            expected = true,
            actual = AnswerPrompt.isNotFound(
                text = "No se puede determinar cuántos libros terminaste ni cuál te gustó más, ya que la lista solo " +
                    "muestra los títulos y su estado de completado, pero no incluye información sobre tus " +
                    "preferencias personales [1]."
            )
        )
    }
}
