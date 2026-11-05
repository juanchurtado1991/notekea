package app.notekea.shell.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import app.notekea.core.presentation.platform.WindowShortcut
import app.notekea.editor.presentation.EditorIntent
import app.notekea.graph.presentation.GraphContent
import app.notekea.graph.presentation.GraphEffect
import app.notekea.graph.presentation.GraphIntent
import app.notekea.search.presentation.index.SearchIndexViewModel
import app.notekea.shell.domain.Workspace
import app.notekea.tree.presentation.VaultTreeState

/** The graph in its tab (`ROADMAP.md` 2.7). */
@Composable
internal fun GraphTab(
    viewModels: WorkspaceViewModels
) {
    val state by viewModels.graph.state.collectAsState()
    GraphContent(state = state, onIntent = viewModels.graph::onIntent)
}

/**
 * What the graph follows while the vault is open, tab shown or not: the vault's notes and its index. `Mod+G` opens it,
 * and a note chosen on it opens in a tab with the keyboard in its text.
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
    LaunchedEffect(graph, state.tree.notes) {
        graph.onIntent(intent = GraphIntent.OnNotesChange(notes = state.tree.notes.values))
    }
    LaunchedEffect(graph, isIndexReady) { graph.onIntent(intent = GraphIntent.OnIndexChange(isReady = isIndexReady)) }
    LaunchedEffect(graph) {
        graph.effects.collect { effect ->
            when (effect) {
                is GraphEffect.OpenNote -> {
                    viewModels.workspace.onIntent(intent = WorkspaceIntent.OnOpen(path = effect.path))
                    val pane = viewModels.workspace.state.value.workspace.paneOpening(path = effect.path)
                    viewModels.editors[pane].onIntent(intent = EditorIntent.OnFocusRequest(path = effect.path))
                }
            }
        }
    }
    WindowShortcut(shortcut = state.platformConventions.graphShortcut) { openGraph(viewModels = viewModels) }
}

/** Opens the graph's tab, around the note being read, or the whole vault when no note is. */
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
