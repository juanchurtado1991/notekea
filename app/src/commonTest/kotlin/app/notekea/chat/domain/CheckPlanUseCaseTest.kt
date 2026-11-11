package app.notekea.chat.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteFixtures.tags
import app.notekea.core.notes.domain.RelativePath
import kotlin.test.Test
import kotlin.test.assertEquals

/** A plan checked against the vault: names found as a person writes them, folders and tags expanded, gaps refused. */
class CheckPlanUseCaseTest {
    private val check = CheckPlanUseCase()

    private val notes = listOf(
        entry(
            path = "Recipes/Arepas.md",
            metadata = tags("food")
        ),
        entry(path = "Recipes/Pancakes.md"),
        entry(
            path = "Books/Dune.md",
            metadata = tags("book")
        ),
        entry(path = "Home.md")
    )

    private fun checked(
        vararg steps: PlannedStep,
        open: String? = null
    ) = check(
        steps = steps.toList(),
        notes = notes,
        openNote = open?.let { RelativePath(value = it) }
    )

    @Test
    fun `notes are found by their names, a folder or a tag stands for its notes, this note is the open one`() {
        val result = checked(
            PlannedStep(
                kind = ActionKind.ADD_TAG,
                fields = mapOf(ActionField.FOLDER to "recipes", ActionField.TAG to "#food")
            ),
            PlannedStep(
                kind = ActionKind.SET_PROPERTY,
                fields = mapOf(
                    ActionField.NOTE to "dune",
                    ActionField.PROPERTY to "rating",
                    ActionField.VALUE to "5"
                )
            ),
            PlannedStep(
                kind = ActionKind.BOOKMARK,
                fields = mapOf(ActionField.NOTE to "this note")
            ),
            open = "Home.md"
        ) as Outcome.Success

        assertEquals(
            expected = listOf(
                listOf("Recipes/Arepas.md", "Recipes/Pancakes.md") to "food",
                listOf("Books/Dune.md") to null,
                listOf("Home.md") to null
            ),
            actual = result.value.map { step ->
                step.notes.map { it.value } to step.tag
            }
        )
    }

    @Test
    fun `a note made by an earlier step can be named by a later one`() {
        val result = checked(
            PlannedStep(
                kind = ActionKind.CREATE_NOTE,
                fields = mapOf(ActionField.TITLE to "Budget", ActionField.FOLDER to "Recipes")
            ),
            PlannedStep(
                kind = ActionKind.SET_PROPERTY,
                fields = mapOf(
                    ActionField.NOTE to "Budget",
                    ActionField.PROPERTY to "status",
                    ActionField.VALUE to "todo"
                )
            )
        ) as Outcome.Success

        assertEquals(
            expected = listOf("Recipes/Budget.md"),
            actual = result
                .value[1]
                .notes
                .map { it.value }
        )
    }

    @Test
    fun `a folder made by an earlier step can be moved into, and an unknown note stops the plan`() {
        val moved = checked(
            PlannedStep(
                kind = ActionKind.CREATE_FOLDER,
                fields = mapOf(ActionField.FOLDER to "Archive")
            ),
            PlannedStep(
                kind = ActionKind.MOVE,
                fields = mapOf(ActionField.NOTE to "Pancakes", ActionField.TARGET to "archive")
            )
        ) as Outcome.Success

        assertEquals(
            expected = listOf<Any?>(
                RelativePath(value = "Archive"),
                Outcome.Failure(error = ActionProblem.UnknownNote(name = "Lasagna")),
                Outcome.Failure(
                    error = ActionProblem.Missing(
                        kind = ActionKind.ADD_TAG,
                        field = ActionField.TAG
                    )
                ),
                Outcome.Failure(error = ActionProblem.Refused(reason = "Settings"))
            ),
            actual = listOf(
                moved.value[1].target,
                checked(
                    PlannedStep(
                        kind = ActionKind.OPEN_NOTE,
                        fields = mapOf(ActionField.NOTE to "Lasagna")
                    )
                ),
                checked(
                    PlannedStep(
                        kind = ActionKind.ADD_TAG,
                        fields = mapOf(ActionField.NOTE to "Dune")
                    )
                ),
                checked(
                    PlannedStep(
                        kind = ActionKind.REFUSE,
                        fields = mapOf(ActionField.TEXT to "Settings")
                    )
                )
            )
        )
    }

    // Agent eval of 2026-11-11: "add a task to buy beans to the recipe…" named the note in the step's list, and the
    // task was planned for today's note instead.
    @Test
    fun `a task or a text for one note may name it in the step's list`() {
        val result = checked(
            PlannedStep(
                kind = ActionKind.ADD_TASK,
                fields = mapOf(ActionField.TEXT to "Buy beans"),
                notes = listOf("Arepas")
            ),
            PlannedStep(
                kind = ActionKind.APPEND_TEXT,
                fields = mapOf(ActionField.TEXT to "More salt."),
                notes = listOf("Dune")
            )
        ) as Outcome.Success

        assertEquals(
            expected = listOf(listOf("Recipes/Arepas.md"), listOf("Books/Dune.md")),
            actual = result.value.map { step ->
                step.notes.map { it.value }
            }
        )
    }
}
