package app.notekea.chat.domain

import app.notekea.chat.domain.answer.AnswerFromFoundUseCase
import app.notekea.chat.domain.answer.AnswerStep
import app.notekea.chat.domain.answer.OverviewAsked
import app.notekea.chat.domain.answer.PropertyPassages
import app.notekea.chat.domain.language.SpokenLanguage
import app.notekea.chat.domain.plan.ActionContext
import app.notekea.chat.domain.plan.ActionField
import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.FoundNotes
import app.notekea.chat.domain.plan.FoundRow
import app.notekea.chat.domain.plan.PlanWords
import app.notekea.chat.domain.plan.PlannedStep
import app.notekea.chat.domain.plan.TaskPeriods
import app.notekea.chat.domain.prompt.OverviewPrompt
import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.ChatRole
import app.notekea.core.notes.domain.frontmatter.NoteProperty
import app.notekea.core.notes.domain.frontmatter.PropertyValue
import kotlinx.coroutines.flow.toList
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The chat's answers compared with Claude's own, read from the same Sample Vault (2026-11-11), each difference as the
 * rule that fixes it: the last note, what Notekea is, a late project and the tables' answer.
 */
class ChatComparisonTest {
    // The comparison with Claude's own answers, 2026-11-11.
    @Test
    fun `the last note, what Notekea is and a late project are planned from the request's words`() {
        val context = ActionContext(
            today = "2026-11-11",
            latestNote = "Journal/Daily/2026-10-25"
        )

        val answer = listOf(PlannedStep(kind = ActionKind.ANSWER))

        val planned = listOf(
            "tráeme la última nota que escribí",
            "no entiendo, explicame de nuevo que es Notekea",
            "quiero que me digas cuál de mis proyectos está más atrasado",
            "move the late notes to Archive"
        ).map {
            PlanWords
                .shaped(
                    steps = answer,
                    request = it,
                    context = context
                )
                .single()
        }

        assertEquals(
            expected = listOf<Any?>(
                ActionKind.OPEN_NOTE to "Journal/Daily/2026-10-25",
                ActionKind.HELP to null,
                ActionKind.SHOW_TASKS to "overdue",
                ActionKind.ANSWER to null
            ),
            actual = planned.map {
                it.kind to (it[ActionField.NOTE] ?: it[ActionField.PERIOD])
            }
        )
    }

    @Test
    fun `a late project asks for overdue tasks, a later one does not`() {
        val tasks = PlannedStep(kind = ActionKind.SHOW_TASKS)

        assertEquals(
            expected = listOf("overdue", "overdue", "later"),
            actual = listOf(
                "which of my projects is late, and what should I do next?",
                "quiero que me digas cuál de mis proyectos está más atrasado",
                "what tasks are due later?"
            ).map {
                TaskPeriods.asked(
                    step = tasks,
                    request = it
                )[ActionField.PERIOD]
            }
        )
    }

    @Test
    fun `the tables' answer says the tasks are overdue and never writes the tables again`() =
        kotlinx
            .coroutines
            .test
            .runTest {
                val engine = app
                    .notekea
                    .core
                    .llm
                    .domain
                    .FakeLlmEngine()

                engine.reply = { "Notekea launch is late: Write the announcement.\n| note | task |\n|---|---|\n| A | B |" }

                val steps = AnswerFromFoundUseCase(llmEngine = engine)(
                    question = "which project is late?",
                    tables = listOf(
                        FoundNotes(
                            isTasks = true,
                            period = "overdue",
                            rows = listOf(
                                FoundRow(
                                    note = app
                                        .notekea
                                        .core
                                        .notes
                                        .domain
                                        .RelativePath(value = "Projects/Notekea launch.md"),
                                    cells = listOf("Write the announcement 2026-10-30"),
                                    due = "overdue"
                                )
                            )
                        )
                    ),
                    today = "2026-11-11"
                ).toList()

                val said = engine.generated.last()

                assertEquals(
                    expected = listOf(true, true, "Notekea launch is late: Write the announcement."),
                    actual = listOf(
                        "(1 tasks overdue, every one of them): note | task" in said.last().text &&
                            "Write the announcement 2026-10-30 (overdue)" in said.last().text,
                        said
                            .first()
                            .text
                            .endsWith("Today is 2026-11-11: a task dated before it is overdue."),
                        (steps.last() as AnswerStep.Done).answer.text
                    )
                )
            }

    @Test
    fun `books asked in Spanish give the books' properties to answer from`() {
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
                                key = "finished",
                                value = PropertyValue
                                    .Checkbox(isChecked = true)
                            )
                        )
                    )
            )

        assertEquals(
            expected = listOf("Dune — finished: true"),
            actual = PropertyPassages
                .of(
                    question = "cuantos libros terminé?",
                    notes = listOf(dune),
                    only = null
                )
                .map { it.text }
        )
    }

    @Test
    fun `a move that lost its target goes to the folder the plan makes or the request names`() {
        val recipes = listOf("Recipes/Arepas", "Recipes/Pancakes")

        val made = listOf(
            PlannedStep(
                kind = ActionKind.CREATE_FOLDER,
                fields = mapOf(ActionField.FOLDER to "Archive")
            ),
            PlannedStep(
                kind = ActionKind.MOVE,
                notes = recipes + "Recipes/Archive (if created implicitly)"
            )
        )

        val named = listOf(
            PlannedStep(
                kind = ActionKind.MOVE,
                notes = recipes
            )
        )

        val context = ActionContext(today = "2026-11-11")

        assertEquals(
            expected = listOf("Archive" to recipes, "Cocina" to recipes),
            actual = listOf(
                PlanWords.targetsNamed(
                    steps = made,
                    context = context
                ).last(),
                PlanWords.targetsNamed(
                    steps = named,
                    context = context,
                    request = "pon todas las recetas en Cocina"
                ).last()
            ).map {
                it[ActionField.TARGET] to it.notes
            }
        )
    }

    @Test
    fun `a due date stays only when the request gives one, and the notes changed today are named`() {
        val task = PlannedStep(
            kind = ActionKind.ADD_TASK,
            fields = mapOf(ActionField.TEXT to "book the flights", ActionField.DUE to "2026-11-11")
        )

        val context = ActionContext(
            today = "2026-11-11",
            changedToday = listOf("Projects/Notekea launch")
        )

        assertEquals(
            expected = listOf<Any?>(null, "2026-11-11", true),
            actual = listOf(
                PlanWords.dueAsked(
                    step = task,
                    request = "add those as tasks here"
                )[ActionField.DUE],
                PlanWords.dueAsked(
                    step = task,
                    request = "add it for today"
                )[ActionField.DUE],
                "Notes changed today: Projects/Notekea launch" in context.described()
            )
        )
    }

    @Test
    fun `the most important notes are the vault's overview, which names the notes marked with a priority`() {
        val launch = app
            .notekea
            .core
            .notes
            .domain
            .NoteFixtures
            .entry(
                path = "Projects/Notekea launch.md",
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
                                key = "priority",
                                value = PropertyValue
                                    .Number(text = "1")
                            )
                        )
                    )
            )

        assertEquals(
            expected = listOf(ActionKind.OVERVIEW, ActionKind.OVERVIEW, true),
            actual = listOf(
                OverviewAsked
                    .shaped(
                        steps = listOf(PlannedStep(kind = ActionKind.ANSWER)),
                        request = "y cuales son las mas importantes?"
                    )
                    .single()
                    .kind,
                OverviewAsked
                    .shaped(
                        steps = listOf(PlannedStep(kind = ActionKind.ANSWER)),
                        request = "which are my most important notes?"
                    )
                    .single()
                    .kind,
                MARKED in OverviewPrompt.mapOf(notes = listOf(launch))
            )
        )
    }

    @Test
    fun `how many books is counted from the properties, and asked in Spanish it is answered in Spanish`() {
        val answer = listOf(PlannedStep(kind = ActionKind.ANSWER))

        assertEquals(
            expected = listOf<Any?>(ActionKind.FIND_NOTES, ActionKind.ANSWER, "Spanish"),
            actual = listOf(
                PlanWords
                    .shaped(
                        steps = answer,
                        request = "cuántos libros terminé?"
                    )
                    .single()
                    .kind,
                PlanWords
                    .shaped(
                        steps = answer,
                        request = "how many servings do the black beans make?"
                    )
                    .single()
                    .kind,
                SpokenLanguage.of(text = "cuántos libros terminé?")
            )
        )
    }

    private companion object {
        const val MARKED = "Marked: Notekea launch (priority: 1)"
    }
}
