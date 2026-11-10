package app.notekea.tasks.domain

import app.notekea.core.notes.domain.RelativePath

/** A group of the Tasks tab, named by its note or by [TaskWhen], and its [rows]. */
sealed interface TaskGroup {
    val rows: List<TaskRow>

    /** A note's tasks in view: [done] of its [total] are checked, whether or not the done ones are among [rows]. */
    data class OfNote(
        val path: RelativePath,
        override val rows: List<TaskRow>,
        val done: Int,
        val total: Int
    ) : TaskGroup

    data class OfDate(val date: TaskWhen, override val rows: List<TaskRow>) : TaskGroup
}
