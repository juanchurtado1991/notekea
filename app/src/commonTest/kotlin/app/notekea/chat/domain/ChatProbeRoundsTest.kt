package app.notekea.chat.domain

import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.ChatRole
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The later rounds of the chat probe of 2026-11-11, each as the rule that fixes it: the chat's language, a not found
 * kept, the overview, a search answered, the plan's wording, a change refused, and words the plan mixed up.
 */
class ChatProbeRoundsTest {
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
    fun `a question about the notes as a whole is the overview, a question about some is not`() {
        val answer = listOf(PlannedStep(kind = ActionKind.ANSWER))

        assertEquals(
            expected = listOf(ActionKind.OVERVIEW, ActionKind.OVERVIEW, ActionKind.ANSWER),
            actual = listOf(
                OverviewAsked.shaped(
                    steps = answer,
                    request = "me podes contar de que tratan las notas?"
                ),
                OverviewAsked.shaped(
                    steps = answer,
                    request = "summarize everything in one line"
                ),
                OverviewAsked.shaped(
                    steps = answer,
                    request = "summarize my October meetings"
                )
            ).map { it.single().kind }
        )
    }

    @Test
    fun `a question planned as a search is answered, a search for notes stays one`() {
        val searched = listOf(
            PlannedStep(
                kind = ActionKind.SEARCH,
                fields = mapOf(ActionField.TEXT to "October 22")
            )
        )

        assertEquals(
            expected = listOf(ActionKind.ANSWER, ActionKind.SEARCH),
            actual = listOf(
                QuestionKept.shaped(
                    steps = searched,
                    request = "what did I cook on October 22?"
                ),
                QuestionKept.shaped(
                    steps = searched,
                    request = "which notes link to Weekly menu?"
                )
            ).map { it.single().kind }
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
    fun `a change refused twice keeps its refusal, and the agent may trash`() {
        assertEquals(
            expected = listOf(true, true, false, false, true),
            actual = listOf(
                RefusalChecked.isChange(request = "delete the note Pancakes"),
                RefusalChecked.isChange(request = "borrá la nota Pancakes"),
                RefusalChecked.isChange(request = "and the water?"),
                RefusalChecked.isChange(request = "how long does the dough rest?"),
                AgentTaskPrompt
                    .messages(
                        task = "delete Pancakes",
                        context = ActionContext(today = "2026-11-11"),
                        found = emptyList(),
                        movesLeft = 3,
                        mustEnd = false
                    )
                    .first()
                    .text
                    .contains("trash (note or notes: a delete)")
            )
        )
    }

    @Test
    fun `what was written, asked without a question word, is answered rather than searched`() {
        val searched = listOf(
            PlannedStep(
                kind = ActionKind.SEARCH,
                fields = mapOf(ActionField.TEXT to "Lisbon")
            )
        )

        assertEquals(
            expected = ActionKind.ANSWER,
            actual = QuestionKept
                .shaped(
                    steps = searched,
                    request = "je voudrais savoir ce que j'ai écrit sur Lisbonne"
                )
                .single()
                .kind
        )
    }

    @Test
    fun `a bookmark asked is made, a topic searched, and a folder named as a note is the folder`() {
        val panel = listOf(
            PlannedStep(
                kind = ActionKind.SHOW_PANEL,
                fields = mapOf(ActionField.PANEL to "bookmarks")
            )
        )

        val found = listOf(PlannedStep(kind = ActionKind.FIND_NOTES))

        val moved = PlannedStep(
            kind = ActionKind.MOVE,
            fields = mapOf(ActionField.NOTE to "Guides", ActionField.TARGET to "Archive")
        )

        val context = ActionContext(
            today = "2026-11-11",
            folders = listOf("Guides"),
            notes = listOf("Recipes/Arepas")
        )

        assertEquals(
            expected = listOf(
                ActionField.NOTE to "Home",
                ActionField.FOLDER to "Projects",
                ActionField.TEXT to "habits",
                ActionField.FOLDER to "Guides"
            ),
            actual = listOf(
                PlanWords
                    .shaped(
                        steps = panel,
                        request = "bookmark Home"
                    )
                    .single()
                    .fields
                    .entries
                    .single(),
                PlanWords
                    .shaped(
                        steps = panel,
                        request = "añade a marcadores la carpeta Projects"
                    )
                    .single()
                    .fields
                    .entries
                    .single(),
                PlanWords
                    .shaped(
                        steps = found,
                        request = "find my notes on habits"
                    )
                    .single()
                    .fields
                    .entries
                    .single(),
                PlanWords
                    .folderNamed(
                        step = moved,
                        context = context
                    )
                    .fields
                    .entries
                    .first {
                        it.value == "Guides"
                    }
            ).map {
                it.key to it.value
            }
        )
    }
}
