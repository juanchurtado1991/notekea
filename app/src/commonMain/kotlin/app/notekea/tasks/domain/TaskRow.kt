package app.notekea.tasks.domain

import app.notekea.core.notes.domain.NoteTask
import app.notekea.core.notes.domain.RelativePath

/** A task in the Tasks tab, with the note at [path] it belongs to and when it is [due]. */
data class TaskRow(
    val path: RelativePath,
    val task: NoteTask,
    val due: TaskWhen
)
