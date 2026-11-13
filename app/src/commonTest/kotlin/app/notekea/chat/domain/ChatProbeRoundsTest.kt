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
import app.notekea.core.notes.domain.frontmatter.NoteProperty
import app.notekea.core.notes.domain.frontmatter.PropertyValue
import kotlinx.coroutines.flow.toList
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The later rounds of the chat probe of 2026-11-11, each as the rule that fixes it: the chat's language, a not found
 * kept, the overview, a search answered, the plan's wording, a change refused, and words the plan mixed up.
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

    @Test
    fun `the tables found are read with every property of their notes`() = kotlinx
        .coroutines
        .test
        .runTest {
            val engine = app
                .notekea
                .core
                .llm
                .domain
                .FakeLlmEngine()

            val dune = app
                .notekea
                .core
                .notes
                .domain
                .NoteFixtures
                .entry(
                    path = "Books/Dune.md",
                    metadata = app
                        .notekea
                        .core
                        .notes
                        .domain
                        .NoteMetadata
                        .NONE
                        .copy(
                            properties = listOf(
                                NoteProperty(
                                    key = "rating",
                                    value = PropertyValue
                                        .Number(text = "5")
                                )
                            )
                        )
                )

            AnswerFromFoundUseCase(llmEngine = engine)(
                question = "which finished book did I like most?",
                tables = listOf(
                    FoundNotes(
                        rows = listOf(
                            FoundRow(
                                note = dune.file.path,
                                cells = listOf("true")
                            )
                        )
                    )
                ),
                notes = listOf(dune)
            ).collect {}

            assertEquals(
                expected = true,
                actual = engine
                    .generated
                    .last()
                    .last()
                    .text
                    .contains("- Dune | true (rating: 5)")
            )
        }

    // The chat probe on Large, 2026-11-11.
    @Test
    fun `a message with no word is talk, and a move's target listed among its notes is its target`() {
        val today = listOf(PlannedStep(kind = ActionKind.OPEN_TODAY))

        val planned = listOf(
            PlannedStep(
                kind = ActionKind.CREATE_FOLDER,
                fields = mapOf(ActionField.FOLDER to "Archive")
            ),
            PlannedStep(
                kind = ActionKind.MOVE,
                notes = listOf("Arepas", "Pancakes", "Archive")
            )
        )

        val moved = PlanWords
            .targetsNamed(
                steps = planned,
                context = ActionContext(today = "2026-11-11")
            )
            .last()

        assertEquals(
            expected = listOf<Any?>(ActionKind.CHAT, "Archive", listOf("Arepas", "Pancakes")),
            actual = listOf(
                PlanWords
                    .shaped(
                        steps = today,
                        request = "?"
                    )
                    .single()
                    .kind,
                moved[ActionField.TARGET],
                moved.notes
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

    @Test
    fun `a joke planned as talk is written from the notes`() {
        assertEquals(
            expected = listOf(ActionKind.ANSWER, ActionKind.CHAT),
            actual = listOf(
                PlanWords.shaped(
                    steps = listOf(PlannedStep(kind = ActionKind.CHAT)),
                    request = "tell me a joke about my notes"
                ),
                PlanWords.shaped(
                    steps = listOf(PlannedStep(kind = ActionKind.CHAT)),
                    request = "hola"
                )
            ).map { it.single().kind }
        )
    }

    @Test
    fun `a complaint is talk, a change asked with it is still planned`() {
        val help = listOf(PlannedStep(kind = ActionKind.HELP))

        val trash = listOf(
            PlannedStep(
                kind = ActionKind.TRASH,
                fields = mapOf(ActionField.NOTE to "Pancakes")
            )
        )

        assertEquals(
            expected = listOf(ActionKind.CHAT, ActionKind.CHAT, ActionKind.TRASH),
            actual = listOf(
                PlanWords.shaped(
                    steps = help,
                    request = "Notekea es una porquería, no sirve para nada"
                ),
                PlanWords.shaped(
                    steps = help,
                    request = "this app is useless"
                ),
                PlanWords.shaped(
                    steps = trash,
                    request = "this note is garbage, delete Pancakes"
                )
            ).map { it.single().kind }
        )
    }
}
