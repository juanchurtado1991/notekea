package app.notekea.shell.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import app.notekea.core.platform.domain.graphShortcut
import app.notekea.core.presentation.platform.WindowShortcut
import app.notekea.editor.presentation.EditorIntent
import app.notekea.graph.presentation.GraphEffect
import app.notekea.graph.presentation.GraphIntent
import app.notekea.search.presentation.index.SearchIndexViewModel
import app.notekea.shell.domain.Workspace
import app.notekea.tree.presentation.VaultTreeState

/**
 * What the graph follows while the vault is open: its notes, its index and whether its tab shows, as it works only
 * then. `Mod+G` opens it, a note chosen on it opens in a tab with the keyboard in its text, and a note it wrote reaches
 * the editor that has it open.
 */
@Composable
internal fun FollowGraph(
    viewModels: WorkspaceViewModels,
    state: VaultTreeState,
    searchIndex: SearchIndexViewModel
) {
    val graph = viewModels.graph
    val index by searchIndex.state.collectAsState()
    val isIndexReady = !index.isRunning && index.readyNotes != null
    val tabs by viewModels.workspace.state.collectAsState()
    val isShown = tabs.workspace.panes.any { it.activePath == Workspace.GRAPH }
    LaunchedEffect(graph, state.tree.notes) {
        graph.onIntent(intent = GraphIntent.OnNotesChange(notes = state.tree.notes.values))
    }
    LaunchedEffect(graph, isIndexReady) { graph.onIntent(intent = GraphIntent.OnIndexChange(isReady = isIndexReady)) }
    LaunchedEffect(graph, isShown) { graph.onIntent(intent = GraphIntent.OnShownChange(isShown = isShown)) }
    LaunchedEffect(graph) {
        graph.effects.collect { effect ->
            when (effect) {
                is GraphEffect.OpenNote -> {
                    viewModels.workspace.onIntent(intent = WorkspaceIntent.OnOpen(path = effect.path))
                    val pane = viewModels.workspace.state.value.workspace.paneOpening(path = effect.path)
                    viewModels.editors[pane].onIntent(intent = EditorIntent.OnFocusRequest(path = effect.path))
                }

                is GraphEffect.NoteRewritten -> viewModels.editors.forEach { editor ->
                    editor.onIntent(intent = EditorIntent.OnNoteRewritten(path = effect.path))
                }
            }
        }
    }
    WindowShortcut(shortcut = state.platformConventions.graphShortcut) { openGraph(viewModels = viewModels) }
}

/** Opens the graph's tab, around the note being read; with none, it asks for one. */
internal fun openGraph(
    viewModels: WorkspaceViewModels
) {
    val workspace = viewModels.workspace
    val tabs = workspace.state.value
    val active = tabs.workspace.activePath
    if (active != Workspace.GRAPH) {
        val note = active?.takeIf { tabs.kindOf(path = it) == TabKind.NOTE }
        viewModels.graph.onIntent(intent = GraphIntent.OnOpen(note = note))
    }
    workspace.onIntent(intent = WorkspaceIntent.OnOpen(path = Workspace.GRAPH))
}
