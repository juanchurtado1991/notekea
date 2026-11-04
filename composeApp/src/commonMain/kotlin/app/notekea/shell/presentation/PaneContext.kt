package app.notekea.shell.presentation

import app.notekea.editor.presentation.EditorPaneTools
import app.notekea.editor.presentation.SidePanelHost
import app.notekea.shell.presentation.WorkspaceState
import app.notekea.notes.presentation.tree.VaultTreeState

/** What every pane's content is drawn from: the tree's and the tabs' states, the view models and each pane's tools. */
internal data class PaneContext(
    val state: VaultTreeState,
    val workspaceState: WorkspaceState,
    val viewModels: WorkspaceViewModels,
    val panes: List<EditorPaneTools>,
    val sidePanel: SidePanelHost
)
