package app.notekea.chat.domain

import app.notekea.chat.domain.answer.AnswerFromFoundUseCase
import app.notekea.chat.domain.answer.OverviewAsked
import app.notekea.chat.domain.answer.QuestionKept
import app.notekea.chat.domain.answer.RefusalChecked
import app.notekea.chat.domain.language.ReplyReworded
import app.notekea.chat.domain.language.SpokenLanguage
import app.notekea.chat.domain.language.inLanguage
import app.notekea.chat.domain.plan.ActionContext
import app.notekea.chat.domain.plan.ActionField
import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.FoundNotes
import app.notekea.chat.domain.plan.FoundRow
import app.notekea.chat.domain.plan.PlanWords
import app.notekea.chat.domain.plan.PlannedStep
import app.notekea.chat.domain.prompt.ActionPrompt
import app.notekea.chat.domain.prompt.AgentTaskPrompt
import app.notekea.chat.domain.prompt.AnswerPrompt
import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.ChatRole
import kotlinx.coroutines.flow.toList
import kotlin.test.Test
import kotlin.test.assertEquals

/** The chat probe's rounds about language: which language a reply is in, and asking for another. */
class ChatProbeLanguageTest {
    @Test
    fun `a message too short to tell is in the chat's language, and a greeting tells alone`() {
        val spanish = listOf(
            ChatExchange(
                question = "¿cuánta agua llevan las arepas?",
                reply = "Dos tazas."
            )
        )

        assertEquals(
            expected = listOf("Spanish", "Spanish", "French", null),
            actual = listOf(
                SpokenLanguage.inChat(
                    text = "ok!",
                    history = spanish
                ),
                SpokenLanguage.inChat(
                    text = "gracias!",
                    history = emptyList()
                ),
                SpokenLanguage.inChat(
                    text = "merci",
                    history = spanish
                ),
                SpokenLanguage.inChat(
                    text = "ok!",
                    history = emptyList()
                )
            )
        )
    }

    @Test
    fun `the person's language and a retry are said before the request, never in it`() {
        val asked = ActionPrompt
            .messages(
                request = "and the water?",
                context = ActionContext(
                    today = "2026-11-11",
                    language = "Spanish"
                ),
                isRetried = true
            )
            .last()
            .text

        assertEquals(
            expected = listOf(true, true, true),
            actual = listOf(
                asked.endsWith("\n\nRequest: and the water?"),
                "The person writes in Spanish." in asked,
                "do not refuse it" in asked.substringBefore(delimiter = "Request:")
            )
        )
    }

    @Test
    fun `only a language asked says the last reply again, a question in a language is answered`() {
        val history = listOf(
            ChatExchange(
                question = "How long does the dough rest?",
                reply = "Five minutes."
            )
        )

        val talk = listOf(
            PlannedStep(
                kind = ActionKind.CHAT,
                fields = mapOf(ActionField.NOTE to "this note")
            )
        )

        val answer = listOf(PlannedStep(kind = ActionKind.ANSWER))

        assertEquals(
            expected = listOf<Any?>(null, ActionKind.CHAT, ActionKind.ANSWER),
            actual = listOf(
                ReplyReworded.shaped(
                    steps = talk,
                    request = "in spanish please",
                    history = history
                )
                    .single()[ActionField.NOTE],
                ReplyReworded
                    .shaped(
                        steps = answer,
                        request = "en inglés",
                        history = history
                    )
                    .single()
                    .kind,
                ReplyReworded
                    .shaped(
                        steps = answer,
                        request = "en español, ¿qué opinas de Dune?",
                        history = history
                    )
                    .single()
                    .kind
            )
        )
    }
}
