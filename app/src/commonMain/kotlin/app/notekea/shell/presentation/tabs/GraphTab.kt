package app.notekea.shell.presentation.tabs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import app.notekea.core.platform.domain.graphShortcut
import app.notekea.core.presentation.platform.WindowShortcut
import app.notekea.editor.presentation.model.EditorIntent
import app.notekea.editor.presentation.sidepanel.SidePanelHost
import app.notekea.graph.presentation.model.GraphEffect
import app.notekea.graph.presentation.model.GraphIntent
import app.notekea.search.presentation.index.SearchIndexViewModel
import app.notekea.shell.domain.Workspace
import app.notekea.shell.presentation.workspace.WorkspaceIntent
import app.notekea.shell.presentation.workspace.WorkspaceViewModels
import app.notekea.tree.presentation.model.VaultTreeState

/**
 * What the graph follows while the vault is open: its notes, its index and whether its tab shows, as it works only
 * then. `Mod+G` opens it, a note chosen on it opens in a tab with the keyboard in its text, and a note it wrote reaches
 * the editor that has it open. A tab with no note, as one restored at launch, takes the note being read beside it.
 */
@Composable
internal fun FollowGraph(
    viewModels: WorkspaceViewModels,
    state: VaultTreeState,
    searchIndex: SearchIndexViewModel,
    sidePanel: SidePanelHost
) {
    val graph = viewModels.graph
    val index by searchIndex.state.collectAsState()
    val isIndexReady = !index.isRunning && index.readyNotes != null

    val tabs by viewModels
        .workspace
        .state
        .collectAsState()

    val isShown = tabs
        .workspace
        .panes
        .any {
            it.activePath == Workspace.GRAPH
        }

    LaunchedEffect(graph, state.tree.notes) {
        graph.onIntent(intent = GraphIntent.OnNotesChange(notes = state.tree.notes.values))
    }

    LaunchedEffect(graph, isIndexReady) {
        graph.onIntent(intent = GraphIntent.OnIndexChange(isReady = isIndexReady))
    }

    LaunchedEffect(graph, isShown) {
        graph.onIntent(intent = GraphIntent.OnShownChange(isShown = isShown))
        if (isShown && graph.state.value.hasNoNote) openAroundNote(viewModels = viewModels)
    }

    LaunchedEffect(graph) {
        graph.effects.collect { effect ->
            when (effect) {
                is GraphEffect.OpenNote -> {
                    viewModels.workspace.onIntent(
                        intent = WorkspaceIntent.OnOpen(path = effect.path)
                    )

                    val pane = viewModels
                        .workspace
                        .state
                        .value
                        .workspace
                        .paneOpening(path = effect.path)

                    viewModels.editors[pane].onIntent(
                        intent = EditorIntent.OnFocusRequest(path = effect.path)
                    )
                }

                GraphEffect.ToggleInspector -> sidePanel.onIntent(
                    intent = EditorIntent.OnSidePanelToggle
                )

                is GraphEffect.NoteRewritten -> viewModels.editors.forEach { editor ->
                    editor.onIntent(intent = EditorIntent.OnNoteRewritten(path = effect.path))
                }
            }
        }
    }

    WindowShortcut(shortcut = state.platformConventions.graphShortcut) {
        openGraph(viewModels = viewModels)
    }
}

/** Opens the graph's tab, around the note being read, in either pane; with none, it asks for one. */
internal fun openGraph(viewModels: WorkspaceViewModels) {
    val workspace = viewModels.workspace
    val isGraphActive = workspace.state.value.workspace.activePath == Workspace.GRAPH
    val isNoteNeeded = !isGraphActive || viewModels.graph.state.value.hasNoNote
    if (isNoteNeeded) openAroundNote(viewModels = viewModels)
    workspace.onIntent(intent = WorkspaceIntent.OnOpen(path = Workspace.GRAPH))
}

private fun openAroundNote(viewModels: WorkspaceViewModels) {
    val note = viewModels.workspace.state.value.noteShown
    viewModels.graph.onIntent(intent = GraphIntent.OnOpen(note = note))
}
