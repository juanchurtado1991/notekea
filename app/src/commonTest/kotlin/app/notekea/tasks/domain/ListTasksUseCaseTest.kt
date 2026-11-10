package app.notekea.tasks.domain

import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteFile
import app.notekea.core.notes.domain.NoteMetadata
import app.notekea.core.notes.domain.NoteTask
import app.notekea.core.notes.domain.RelativePath
import kotlin.test.Test
import kotlin.test.assertEquals

/** What the Tasks tab lists. */
class ListTasksUseCaseTest {
    private val list = ListTasksUseCase()

    private fun titles(result: TaskList): List<Pair<Any, List<String>>> =
        result.groups.map { group ->
            val title: Any =
                if (group is TaskGroup.OfDate) group.date else (group as TaskGroup.OfNote).path.name

            title to group.rows.map { it.task.text }
        }

    @Test
    fun `by note lists open tasks in name order, each note's in its order, and counts the done ones`() {
        val result = list(TaskView(), NOTES, TODAY)

        assertEquals(
            expected = listOf<Pair<Any, List<String>>>(
                "Launch.md" to listOf("Old", "Now", "Soon", "Far", "Whenever"),
                "Trip.md" to listOf("Book")
            ),
            actual = titles(result = result)
        )

        assertEquals(
            expected = 6 to 7,
            actual = result.open to result.total
        )
    }

    @Test
    fun `a note's card counts its done tasks though they are hidden, and each task knows when it is due`() {
        val launch = list(TaskView(), NOTES, TODAY).groups.first() as TaskGroup.OfNote

        assertEquals(
            expected = Triple(
                1,
                6,
                listOf(
                    TaskWhen.OVERDUE,
                    TaskWhen.TODAY,
                    TaskWhen.THIS_WEEK,
                    TaskWhen.LATER,
                    TaskWhen.NO_DATE
                )
            ),
            actual = Triple(launch.done, launch.total, launch.rows.map { it.due })
        )
    }

    @Test
    fun `by date splits overdue, today, this week, later and no date, with the done ones last`() {
        val result = list(
            TaskView(
                grouping = TaskGrouping.DATE,
                showsDone = true
            ),
            NOTES,
            TODAY
        )

        assertEquals(
            expected = listOf<Pair<Any, List<String>>>(
                TaskWhen.OVERDUE to listOf("Old"),
                TaskWhen.TODAY to listOf("Now"),
                TaskWhen.THIS_WEEK to listOf("Soon"),
                TaskWhen.LATER to listOf("Far"),
                TaskWhen.NO_DATE to listOf("Whenever", "Book"),
                TaskWhen.DONE to listOf("Finished")
            ),
            actual = titles(result = result)
        )
    }

    @Test
    fun `a folder or a tag limits the tasks and their count, and what can be chosen stays`() {
        val inFolder = list(TaskView(folder = "Travel"), NOTES, TODAY)
        val tagged = list(TaskView(tag = "work"), NOTES, TODAY)

        assertEquals(
            expected = listOf("Book"),
            actual = inFolder.groups.flatMap { g ->
                g.rows.map { it.task.text }
            }
        )

        assertEquals(
            expected = 1 to 1,
            actual = inFolder.open to inFolder.total
        )

        assertEquals(
            expected = 5 to 6,
            actual = tagged.open to tagged.total
        )

        assertEquals(
            expected = listOf("Projects", "Travel"),
            actual = inFolder.folders
        )

        assertEquals(
            expected = listOf("work"),
            actual = inFolder.tags
        )
    }

    @Test
    fun `a tag limit takes the tags nested under it, whatever their case`() {
        val nested = note(
            path = "Inbox.md",
            tasks = listOf(task(text = "Nested")),
            tags = listOf("Work/Urgent")
        )

        val result = list(TaskView(tag = "work"), listOf(nested), TODAY)

        assertEquals(
            expected = 1,
            actual = result.total
        )
    }

    @Test
    fun `no note with tasks is no group`() {
        val result = list(TaskView(), listOf(note(path = "Empty.md")), TODAY)

        assertEquals(
            expected = TaskList(),
            actual = result
        )
    }

    private companion object {
        val TODAY = CalendarDate(
            year = 2026,
            month = 11,
            day = 6
        )

        fun task(
            text: String,
            days: Long? = null,
            isDone: Boolean = false
        ) = NoteTask(
            stateIndex = text.length,
            endIndex = text.length + 1,
            text = text,
            isDone = isDone,
            date = days?.let { TODAY.plusDays(days = it) }
        )

        fun note(
            path: String,
            tasks: List<NoteTask> = emptyList(),
            tags: List<String> = emptyList()
        ) = NoteEntry(
            file = NoteFile(
                path = RelativePath(value = path),
                size = null,
                modifiedAt = null
            ),
            metadata = NoteMetadata.NONE.copy(
                tags = tags,
                tasks = tasks
            )
        )

        val NOTES = listOf(
            note(
                path = "Travel/Trip.md",
                tasks = listOf(task(text = "Book"))
            ),
            note(
                path = "Projects/Launch.md",
                tags = listOf("work"),
                tasks = listOf(
                    task(
                        text = "Old",
                        days = -1
                    ),
                    task(
                        text = "Now",
                        days = 0
                    ),
                    task(
                        text = "Soon",
                        days = 7
                    ),
                    task(
                        text = "Far",
                        days = 8
                    ),
                    task(text = "Whenever"),
                    task(
                        text = "Finished",
                        days = -9,
                        isDone = true
                    )
                )
            )
        )
    }
}
