package app.notekea.shell.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import app.notekea.editor.presentation.EditorIntent
import app.notekea.shell.domain.Workspace
import app.notekea.tasks.presentation.TasksEffect
import app.notekea.tasks.presentation.TasksIntent
import app.notekea.tree.presentation.VaultTreeState

/**
 * What the Tasks tab follows while the vault is open: its notes and whether the tab shows, as it runs only then. A task
 * chosen opens its note with the task selected, and a note a task wrote reaches the editor that has it open.
 */
@Composable
internal fun FollowTasks(
    viewModels: WorkspaceViewModels,
    state: VaultTreeState
) {
    val tasks = viewModels.tasks
    val tabs by viewModels.workspace.state.collectAsState()
    val isShown = tabs.workspace.panes.any { it.activePath == Workspace.TASKS }
    LaunchedEffect(tasks, state.tree.notes) {
        tasks.onIntent(intent = TasksIntent.OnNotesChange(notes = state.tree.notes.values))
    }
    LaunchedEffect(tasks, isShown) { tasks.onIntent(intent = TasksIntent.OnShownChange(isShown = isShown)) }
    LaunchedEffect(tasks) {
        tasks.effects.collect { effect ->
            when (effect) {
                is TasksEffect.OpenNote -> {
                    viewModels.workspace.onIntent(intent = WorkspaceIntent.OnOpen(path = effect.path))
                    val pane = viewModels.workspace.state.value.workspace.paneOpening(path = effect.path)
                    viewModels.editors[pane].onIntent(
                        intent = EditorIntent.OnRevealRequest(path = effect.path, range = effect.range)
                    )
                }

                is TasksEffect.NoteRewritten -> viewModels.editors.forEach { editor ->
                    editor.onIntent(intent = EditorIntent.OnNoteRewritten(path = effect.path))
                }
            }
        }
    }
}
