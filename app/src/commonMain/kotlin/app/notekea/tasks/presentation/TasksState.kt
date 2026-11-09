package app.notekea.tasks.presentation

import app.notekea.tasks.domain.TaskList
import app.notekea.tasks.domain.TaskView

/** The Tasks tab: what it is set to show and what it [found][list]. */
data class TasksState(
    val view: TaskView = TaskView(),
    val list: TaskList = TaskList(),
    val isLoading: Boolean = true,
    val isWriteFailed: Boolean = false
)
