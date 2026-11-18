package app.notekea.chat.domain.tool.change

import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.PlannedStep
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A new note's notes are what it is written from, not what it changes: the loop's fifth run asked which garden note
 * for "a note Garden budget", and the review dropped every source of eight notes the 4B wrote, so none was written.
 */
class WriteNoteSourcesTest {
    private val context = ToolContext(
        vaultPath = VAULT,
        notes = listOf("Projects/Garden plan.md", "Projects/Budget 2026.md").map {
            entry(path = it)
        },
        task = "write a note Garden budget with what the garden was planned to cost and what has been spent",
        openNote = null
    )

    @Test
    fun `a note written from two notes for the garden is added without asking which one`() =
        runTest {
            val result = ChangeTools.writeNote.run(
                call = ToolCall(
                    name = ChangeTools.writeNote.name,
                    arguments = mapOf(
                        "title" to "Garden budget",
                        "notes" to "Projects/Garden plan\nProjects/Budget 2026",
                        "text" to "The garden's planned cost and what was spent."
                    )
                ),
                context = context
            )

            assertEquals(
                expected = listOf(true, 1),
                actual = listOf(
                    result.forModel.startsWith(prefix = "write_note: added"),
                    context.work.changes.size
                )
            )
        }

    @Test
    fun `dropping notes keeps a new note's sources, and still drops the notes a change acts on`() {
        val work = context.work

        work.change(
            step = PlannedStep(
                kind = ActionKind.WRITE_FROM_NOTES,
                notes = listOf("Garden plan", "Budget 2026")
            )
        )

        work.change(
            step = PlannedStep(
                kind = ActionKind.ADD_TAG,
                notes = listOf("Garden plan", "Budget 2026")
            )
        )

        work.drop {
            it == "Budget 2026"
        }

        assertEquals(
            expected = listOf(listOf("Garden plan", "Budget 2026"), listOf("Garden plan")),
            actual = work.changes.map { it.notes }
        )
    }
}
