package app.notekea.tasks.domain

/**
 * What the Tasks tab shows: its [groups], how many of the [total] tasks in view are [open], and the [folders] and [tags]
 * it can be limited to.
 */
data class TaskList(
    val groups: List<TaskGroup> = emptyList(),
    val open: Int = 0,
    val total: Int = 0,
    val folders: List<String> = emptyList(),
    val tags: List<String> = emptyList()
)
