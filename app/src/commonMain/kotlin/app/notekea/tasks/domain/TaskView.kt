package app.notekea.tasks.domain

/** What the Tasks tab shows: how it groups them, the [folder] and the [tag] they are limited to, and the done ones. */
data class TaskView(
    val grouping: TaskGrouping = TaskGrouping.NOTE,
    val folder: String? = null,
    val tag: String? = null,
    val showsDone: Boolean = false
)
