package app.notekea.tasks.presentation

import app.notekea.core.notes.domain.NoteEntry
import app.notekea.tasks.domain.TaskGrouping
import app.notekea.tasks.domain.TaskRow

/** What can happen to the Tasks tab: the vault changes, its tab shows, its controls are used, or a task is used. */
sealed interface TasksIntent {
    data class OnNotesChange(val notes: Collection<NoteEntry>) : TasksIntent

    data class OnShownChange(val isShown: Boolean) : TasksIntent

    data class OnGroupingChange(val grouping: TaskGrouping) : TasksIntent

    data class OnFolderChange(val folder: String?) : TasksIntent

    data class OnTagChange(val tag: String?) : TasksIntent

    data object OnShowDoneToggle : TasksIntent

    data class OnTaskToggle(val row: TaskRow) : TasksIntent

    data class OnTaskOpen(val row: TaskRow) : TasksIntent

    data object OnNoticeDismiss : TasksIntent
}
