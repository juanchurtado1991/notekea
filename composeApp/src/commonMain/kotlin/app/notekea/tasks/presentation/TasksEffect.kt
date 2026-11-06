package app.notekea.tasks.presentation

import app.notekea.core.notes.domain.RelativePath

/** What the Tasks tab asks of the window around it. */
sealed interface TasksEffect {
    /** Opens the note at [path] with its [range], the task, selected. */
    data class OpenNote(val path: RelativePath, val range: IntRange) : TasksEffect

    /** The note at [path] was written, for the editors that have it open. */
    data class NoteRewritten(val path: RelativePath) : TasksEffect
}
