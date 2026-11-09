package app.notekea.tasks.domain

import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.notes.domain.NaturalNameComparator
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteTask

/**
 * The vault's tasks as the Tasks tab shows them, from the note cache: by note, in name order, each
 * note's in its text's order; or by date, the earliest first, with [TaskWhen.THIS_WEEK] the seven days after [today].
 */
class ListTasksUseCase {
    private val names = NaturalNameComparator()

    operator fun invoke(
        view: TaskView,
        notes: Collection<NoteEntry>,
        today: CalendarDate
    ): TaskList {
        val withTasks = notes.filter {
            it
                .metadata
                .tasks
                .isNotEmpty()
        }

        val inView = withTasks.filter {
            isInView(
                note = it,
                view = view
            )
        }.sortedWith(compareBy(names) { it.file.path.nameWithoutExtension })

        val all = inView.flatMap { note ->
            note
                .metadata
                .tasks
                .map {
                    TaskRow(
                        path = note.file.path,
                        task = it
                    )
                }
        }

        val shown = all.filter {
            view.showsDone || !it.task.isDone
        }

        val groups = if (view.grouping == TaskGrouping.NOTE) {
            byNote(shown)
        } else {
            byDate(
                rows = shown,
                today = today
            )
        }

        return TaskList(
            groups = groups,
            open = all.count {
                !it.task.isDone
            },
            total = all.size,
            folders = withTasks
                .flatMapTo(destination = HashSet()) { it.folders }
                .sortedWith(names),
            tags = withTasks
                .flatMapTo(destination = HashSet()) { it.allTags }
                .sortedWith(names)
        )
    }

    private fun isInView(
        note: NoteEntry,
        view: TaskView
    ): Boolean = (view.folder == null || view.folder in note.folders) &&
        (view.tag?.let(note::hasTag) ?: true)

    private fun byNote(rows: List<TaskRow>): List<TaskGroup> = rows.groupBy {
        it.path
    }.map { (path, found) ->
        TaskGroup.OfNote(
            path = path,
            rows = found
        )
    }

    private fun byDate(
        rows: List<TaskRow>,
        today: CalendarDate
    ): List<TaskGroup> {
        val groups = rows.groupBy {
            whenOf(
                task = it.task,
                today = today
            )
        }

        return TaskWhen.entries.mapNotNull { date ->
            groups[date]?.let { found ->
                TaskGroup.OfDate(
                    date = date,
                    rows = found.sortedBy { it.task.date }
                )
            }
        }
    }

    private fun whenOf(
        task: NoteTask,
        today: CalendarDate
    ): TaskWhen {
        val date = task.date

        return when {
            task.isDone -> TaskWhen.DONE
            date == null -> TaskWhen.NO_DATE
            date < today -> TaskWhen.OVERDUE
            date == today -> TaskWhen.TODAY
            date <= today.plusDays(days = WEEK_DAYS) -> TaskWhen.THIS_WEEK
            else -> TaskWhen.LATER
        }
    }

    private companion object {
        const val WEEK_DAYS = 7L
    }
}
