package app.notekea.chat.domain.tool

import app.notekea.chat.domain.FakeActionRunner
import app.notekea.chat.domain.plan.FoundNotes
import app.notekea.chat.domain.plan.FoundRow
import app.notekea.chat.domain.tool.change.ChangeTools
import app.notekea.chat.domain.tool.lookup.TasksTool
import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.notes.domain.NoteFixtures.VAULT
import app.notekea.core.notes.domain.NoteFixtures.entry
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The fixes of "Why the loop fails" (`docs/agent-loop.md`, 2026-11-15): a change tool called for what another one
 * does is told once which to call, and the tasks tool gives each task's date and keeps those of a range.
 */
class ToolMisuseTest {
    private val runner = FakeActionRunner()

    private fun contextOf(task: String) = ToolContext(
        vaultPath = VAULT,
        notes = listOf(
            entry(path = "Projects/Kitchen renovation.md"),
            entry(path = "Meetings/2026-10-21 Kickoff.md")
        ),
        task = task,
        openNote = null
    )

    private suspend fun said(
        tool: ChatTool,
        task: String,
        vararg arguments: Pair<String, String>
    ): String {
        val context = contextOf(task = task)

        val call = ToolCall(
            name = tool.name,
            arguments = arguments.toMap()
        )

        return tool.run(
            call = call,
            context = context
        ).forModel
    }

    @Test
    fun `a note the task says what to hold is pointed to write_note, an empty one is made`() =
        runTest {
            val filled = said(
                tool = ChangeTools.createNote,
                task = "write a note Authors listing every book with its author",
                "title" to "Authors"
            )

            val empty = said(
                tool = ChangeTools.createNote,
                task = "create a note Shopping",
                "title" to "Shopping"
            )

            assertTrue(actual = filled.startsWith(prefix = "create_note: not added. create_note makes an empty"))
            assertTrue(actual = empty.startsWith(prefix = "create_note: added."))
        }

    @Test
    fun `a task appended as text is pointed to add_task`() = runTest {
        val appended = said(
            tool = ChangeTools.append,
            task = "add a task 'buy basil seeds' to the kitchen renovation",
            "note" to "Projects/Kitchen renovation",
            "text" to "- [ ] buy basil seeds"
        )

        assertTrue(actual = "call add_task" in appended)
    }

    @Test
    fun `complete_task where a status is set or a task added is pointed to the tool for it`() =
        runTest {
            val status = said(
                tool = ChangeTools.completeTask,
                task = "pon status done en el proyecto que vence el 15 de noviembre",
                "note" to "Projects/Kitchen renovation",
                "text" to "finish"
            )

            val added = said(
                tool = ChangeTools.completeTask,
                task = "the kickoff meeting has an open task for Ana; add 'review the announcement'",
                "note" to "Meetings/2026-10-21 Kickoff",
                "text" to "review the announcement"
            )

            assertTrue(actual = "call set_property" in status)
            assertTrue(actual = "add_task adds a task" in added)
        }

    @Test
    fun `the tasks tool gives each task's date and keeps only those of the range asked`() =
        runTest {
            runner.found = FoundNotes(
                isTasks = true,
                rows = listOf(
                    row(
                        text = "book the moving truck",
                        date = CalendarDate(
                            year = 2027,
                            month = 1,
                            day = 15
                        )
                    ),
                    row(
                        text = "renew the passport",
                        date = CalendarDate(
                            year = 2027,
                            month = 2,
                            day = 3
                        )
                    ),
                    row(
                        text = "call the plumber",
                        date = null
                    )
                )
            )

            val tasks = TasksTool(actionRunner = runner)

            val january = said(
                tasks,
                "my open tasks due in January 2027",
                "from" to "2027-01-01",
                "to" to "2027-01-31"
            )

            val all = said(tasks, "my open tasks")

            assertEquals(
                expected = "show_tasks :\n- \"Projects/Move\" book the moving truck (later, due 2027-01-15 (Friday))",
                actual = january
            )

            assertEquals(
                expected = 3,
                actual = all.lines().count { it.startsWith(prefix = "- ") }
            )
        }

    private fun row(
        text: String,
        date: CalendarDate?
    ) = FoundRow(
        note = RelativePath(value = "Projects/Move.md"),
        cells = listOf(text),
        due = "later",
        date = date
    )
}
