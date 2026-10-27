package app.notekea.notes.presentation.tree

import app.notekea.notes.presentation.editor.EditorPaneTools
import app.notekea.notes.presentation.editor.SidePanelHost
import app.notekea.notes.presentation.workspace.WorkspaceState

/** What every pane's content is drawn from: the tree's and the tabs' states, the view models and each pane's tools. */
internal data class PaneContext(
    val state: VaultTreeState,
    val workspaceState: WorkspaceState,
    val viewModels: WorkspaceViewModels,
    val panes: List<EditorPaneTools>,
    val sidePanel: SidePanelHost
)
