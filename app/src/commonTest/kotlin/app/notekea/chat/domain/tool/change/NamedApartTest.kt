package app.notekea.chat.domain.tool.change

import app.notekea.chat.domain.loop.FrameCount
import app.notekea.chat.domain.loop.TaskFrame
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.NoteMetadata
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A change to one of two notes that share a name is not held when the task tells it apart by a word only it is
 * called by, a folder, its title or an alias: the E4B's dev round of 2026-11-25 asked "Lab/Budget or Travel/Budget?"
 * for "mark the formalin as paid in the lab budget".
 */
class NamedApartTest {
    private val budgets = listOf(
        titled(
            path = "Lab/Budget.md",
            title = "Lab consumables budget 2026-27"
        ),
        titled(
            path = "Travel/Budget.md",
            title = "Travel budget 2026-27"
        )
    )

    private val team = listOf(
        titled(
            path = "Admin/Budget.md",
            title = "Team budget 2026: conferences and tools"
        ),
        titled(
            path = "Hiring/Budget.md",
            title = "Hiring budget"
        )
    )

    private fun titled(
        path: String,
        title: String
    ): NoteEntry = entry(
        path = path,
        metadata = NoteMetadata.NONE.copy(title = title)
    )

    private suspend fun said(
        task: String,
        note: String,
        notes: List<NoteEntry> = budgets
    ): String = ChangeTools
        .setProperty
        .run(
            call = ToolCall(
                name = ChangeTools.setProperty.name,
                arguments = mapOf(
                    "notes" to note,
                    "property" to "status",
                    "value" to "paid"
                )
            ),
            context = ToolContext(
                vaultPath = VAULT,
                notes = notes,
                task = task,
                openNote = null,
                frame = TaskFrame(
                    targets = emptyList(),
                    count = FrameCount.ONE,
                    candidates = emptyList()
                )
            )
        )
        .forModel
        .substringBefore(delimiter = ".")

    // The path of the note NamedApart tells apart in [task], if one.
    private fun toldOf(
        task: String,
        notes: List<NoteEntry>
    ): String? = NamedApart
        .of(
            task = task,
            notes = notes
        )
        ?.file
        ?.path
        ?.value

    @Test
    fun `a note the task tells apart by its folder or its title is changed, and one it does not is asked about`() =
        runTest {
            assertEquals(
                expected = listOf(
                    "set_property: added",
                    "set_property: added",
                    "set_property: not added yet",
                    "set_property: not added yet"
                ),
                actual = listOf(
                    said(
                        task = "set the formalin's status to paid in the lab budget",
                        note = "Lab/Budget"
                    ),
                    said(
                        task = "set the Lintora invoice's status to paid in the team budget",
                        note = "Admin/Budget",
                        notes = team
                    ),
                    said(
                        task = "set the formalin's status to paid in the budget",
                        note = "Lab/Budget"
                    ),
                    said(
                        task = "set the formalin's status to paid in the lab budget",
                        note = "Travel/Budget"
                    )
                )
            )
        }

    @Test
    fun `a short word of a title tells no note apart, only one of a folder`() {
        val notes = listOf(
            titled(
                path = "Old/Plan.md",
                title = "Plan and the rest"
            ),
            titled(
                path = "New/Plan.md",
                title = "Plan"
            )
        )

        assertEquals(
            expected = listOf("New/Plan.md", null),
            actual = listOf(
                toldOf(
                    task = "add a line to the new plan",
                    notes = notes
                ),
                toldOf(
                    task = "add this and that to the plan",
                    notes = notes
                )
            )
        )
    }
}
