package app.notekea.tasks.presentation

import app.notekea.core.domain.AppDispatchers
import app.notekea.core.domain.time.LocalCalendar
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.presentation.BaseViewModel
import app.notekea.tasks.domain.ListTasksUseCase
import app.notekea.tasks.domain.SetTaskDoneUseCase
import app.notekea.tasks.domain.TaskRow
import app.notekea.tasks.domain.TaskView
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.time.Clock

/**
 * The Tasks tab (`ROADMAP.md` 2.11) for the vault at [vaultPath]: every change lists the tasks again off the UI thread,
 * and nothing runs while its tab is closed. A task checked here shows at once, before the vault's scan brings it.
 */
class TasksViewModel(
    private val vaultPath: String,
    private val listTasks: ListTasksUseCase,
    private val setTaskDone: SetTaskDoneUseCase,
    private val clock: Clock,
    private val localCalendar: LocalCalendar,
    private val dispatchers: AppDispatchers
) : BaseViewModel<TasksState, TasksIntent, TasksEffect>(initialState = TasksState()) {
    private val logger = Logger.withTag(tag = LOG_TAG)
    private val writes = Mutex()
    private var notes: Collection<NoteEntry> = emptyList()
    private var isShown = false
    private var refreshJob: Job? = null

    override fun handleIntent(
        intent: TasksIntent
    ) {
        when (intent) {
            is TasksIntent.OnNotesChange -> {
                notes = intent.notes
                refresh()
            }

            is TasksIntent.OnShownChange -> {
                isShown = intent.isShown
                refresh()
            }

            is TasksIntent.OnGroupingChange -> change { it.copy(grouping = intent.grouping) }

            is TasksIntent.OnFolderChange -> change { it.copy(folder = intent.folder) }

            is TasksIntent.OnTagChange -> change { it.copy(tag = intent.tag) }

            TasksIntent.OnShowDoneToggle -> change { it.copy(showsDone = !it.showsDone) }

            is TasksIntent.OnTaskToggle -> toggle(row = intent.row)

            is TasksIntent.OnTaskOpen -> emit(effect = openEffectOf(row = intent.row))

            TasksIntent.OnNoticeDismiss -> reduce { it.copy(isWriteFailed = false) }
        }
    }

    private fun openEffectOf(
        row: TaskRow
    ): TasksEffect {
        val textStart = minOf(row.task.stateIndex + BOX_END, row.task.endIndex)
        return TasksEffect.OpenNote(path = row.path, range = textStart until row.task.endIndex)
    }

    private fun change(
        update: (TaskView) -> TaskView
    ) {
        reduce { it.copy(view = update(it.view)) }
        refresh()
    }

    private fun refresh() {
        if (!isShown) return
        val view = state.value.view
        val shown = notes
        val today = localCalendar.momentOf(instant = clock.now()).date
        refreshJob?.cancel()
        refreshJob = launchGuarded(onError = ::logUnexpectedError) {
            val list = withContext(context = dispatchers.default) { listTasks(view, shown, today) }
            reduce { it.copy(list = list, isLoading = false) }
        }
    }

    private fun toggle(
        row: TaskRow
    ) {
        launchGuarded(onError = ::showFailure) {
            // In turn: a second click on the same note must read what the first one wrote.
            val rewrite = writes.withLock { setTaskDone(vaultPath = vaultPath, row = row) }
            if (rewrite == null) {
                reduce { it.copy(isWriteFailed = true) }
            } else {
                emit(effect = TasksEffect.NoteRewritten(path = row.path))
                notes = notes.map { if (it.file.path == row.path) toggled(entry = it, row = row) else it }
                refresh()
            }
        }
    }

    private fun toggled(
        entry: NoteEntry,
        row: TaskRow
    ): NoteEntry {
        val tasks = entry.metadata.tasks.map { task ->
            if (task.stateIndex == row.task.stateIndex) task.copy(isDone = !row.task.isDone) else task
        }
        return entry.copy(metadata = entry.metadata.copy(tasks = tasks))
    }

    private fun showFailure(
        throwable: Throwable
    ) {
        logUnexpectedError(throwable = throwable)
        reduce { it.copy(isWriteFailed = true) }
    }

    private fun logUnexpectedError(
        throwable: Throwable
    ) {
        logger.e(throwable = throwable) { UNEXPECTED_ERROR }
    }

    private companion object {
        const val BOX_END = 3
        const val LOG_TAG = "Tasks"
        const val UNEXPECTED_ERROR = "Unexpected tasks failure"
    }
}
