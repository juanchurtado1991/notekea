package app.notekea.tasks.domain

import app.notekea.core.notes.domain.RelativePath

/** A group of the Tasks tab, named by its note or by [TaskWhen], and its [rows]. */
sealed interface TaskGroup {
    val rows: List<TaskRow>

    data class OfNote(val path: RelativePath, override val rows: List<TaskRow>) : TaskGroup

    data class OfDate(val date: TaskWhen, override val rows: List<TaskRow>) : TaskGroup
}
