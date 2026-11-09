package app.notekea.shell.presentation

import app.notekea.editor.presentation.sidepanel.SidePanelHost
import app.notekea.editor.presentation.view.EditorPaneTools
import app.notekea.tree.presentation.model.VaultTreeState

/** What every pane's content is drawn from: the tree's and the tabs' states, the view models and each pane's tools. */
internal data class PaneContext(
    val state: VaultTreeState,
    val workspaceState: WorkspaceState,
    val viewModels: WorkspaceViewModels,
    val panes: List<EditorPaneTools>,
    val sidePanel: SidePanelHost
)
