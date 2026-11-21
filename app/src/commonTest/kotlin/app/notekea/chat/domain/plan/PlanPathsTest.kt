package app.notekea.chat.domain.plan

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteFixtures.tags
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.tasks.NoteTask
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Where a plan's later steps find what its earlier ones made or moved, and how it writes a tag and picks a task (the
 * bug hunt of 2026-11-17, T4, A6, A7, A12): a new note was registered under its raw title, a written note not at all,
 * a moved note at its old path, and a tag with a space broke a tag list.
 */
class PlanPathsTest {
    private val check = CheckPlanUseCase()

    private val notes = listOf(
        entry(path = "Retro.md"),
        entry(
            path = "Trips/Lisbon.md",
            metadata = tags("Road-Trip")
        )
    )

    private fun pathsOf(vararg steps: PlannedStep): List<List<String>> = (
        check(
            steps = steps.toList(),
            notes = notes,
            openNote = null
        ) as Outcome.Success
        )
        .value
        .map { step ->
            step.notes.map { it.value }
        }

    private fun step(
        kind: ActionKind,
        vararg fields: Pair<ActionField, String>,
        notes: List<String> = emptyList()
    ) = PlannedStep(
        kind = kind,
        fields = fields.toMap(),
        notes = notes
    )

    @Test
    fun `a later step finds a new note by its file's name, a written note, and a note after its move`() {
        val paths = pathsOf(
            step(ActionKind.CREATE_NOTE, TITLE to "Ideas?"),
            tagged(name = "Ideas"),
            step(ActionKind.WRITE_FROM_NOTES, TITLE to "Summary", ActionField.TEXT to "a summary"),
            tagged(name = "Summary"),
            step(ActionKind.MOVE, ActionField.TARGET to "Archive", notes = RETRO),
            tagged(name = "Retro"),
            step(ActionKind.RENAME, TITLE to "Retro 2026", notes = RETRO),
            tagged(name = "Retro 2026")
        )

        assertEquals(
            expected = listOf(
                "Ideas.md",
                "Summary.md",
                "Archive/Retro.md",
                "Archive/Retro 2026.md"
            ),
            actual = paths
                .filterIndexed { index, _ ->
                    index % 2 == 1
                }
                .map { it.single() }
        )
    }

    private fun tagged(name: String) = step(
        ActionKind.ADD_TAG,
        TAG to "x",
        notes = listOf(name)
    )

    @Test
    fun `a tag is written without its marks and spaces, and found on the notes in any case`() {
        val checked = check(
            steps = listOf(
                step(ActionKind.ADD_TAG, TAG to "## road trip", notes = RETRO),
                step(ActionKind.SET_PROPERTY, PROPERTY to "s", VALUE to "x", TAG to UPPER)
            ),
            notes = notes,
            openNote = null
        ) as Outcome.Success

        assertEquals(
            expected = listOf("road-trip", "Trips/Lisbon.md"),
            actual = listOf(
                checked
                    .value
                    .first()
                    .tag,
                checked
                    .value
                    .last()
                    .notes
                    .single()
                    .value
            )
        )
    }

    @Test
    fun `tasks that read alike and differ are several, identical lines one, and Markdown is no word`() {
        val written =
            listOf("Call mom", "Call dentist", "Water plants", "Water plants", "Pay **rent**")

        val tasks = written.map {
            NoteTask(
                stateIndex = 0,
                endIndex = 0,
                text = it,
                isDone = false
            )
        }

        assertEquals(
            expected = listOf<Any>(
                TaskMatch.Several(texts = listOf("Call mom", "Call dentist")),
                TaskMatch.One(task = tasks[2]),
                TaskMatch.One(task = tasks[4]),
                TaskMatch.None
            ),
            actual = listOf("call", "water plants", "Pay rent 📅 2027-01-05", "gym").map {
                TaskMatch.of(
                    tasks = tasks,
                    wanted = it
                )
            }
        )
    }

    private companion object {
        const val UPPER = "#ROAD-TRIP"
        val RETRO = listOf("Retro")
        val TITLE = ActionField.TITLE
        val TAG = ActionField.TAG
        val PROPERTY = ActionField.PROPERTY
        val VALUE = ActionField.VALUE
    }
}
