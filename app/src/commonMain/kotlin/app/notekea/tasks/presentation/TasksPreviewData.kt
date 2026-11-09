package app.notekea.tasks.presentation

import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.notes.domain.NoteTask
import app.notekea.core.notes.domain.RelativePath
import app.notekea.tasks.domain.TaskGroup
import app.notekea.tasks.domain.TaskList
import app.notekea.tasks.domain.TaskRow

/** A few tasks for the Tasks tab's previews. */
internal object TasksPreviewData {
    private val LAUNCH = RelativePath(value = "Projects/Notekea launch.md")

    val row = TaskRow(
        path = LAUNCH,
        task = NoteTask(
            stateIndex = 3,
            endIndex = 30,
            text = "Write the announcement",
            isDone = false,
            heading = "Launch plan",
            date = CalendarDate(
                year = 2026,
                month = 10,
                day = 30
            )
        )
    )

    val state = TasksState(
        list = TaskList(
            groups = listOf(
                TaskGroup.OfNote(
                    path = LAUNCH,
                    rows = listOf(row)
                )
            ),
            open = 1,
            total = 2,
            folders = listOf("Projects"),
            tags = listOf("launch")
        ),
        isLoading = false
    )
}
