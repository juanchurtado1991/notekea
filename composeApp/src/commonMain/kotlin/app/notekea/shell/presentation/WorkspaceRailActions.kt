package app.notekea.shell.presentation

import app.notekea.core.presentation.platform.WindowDialog
import app.notekea.editor.presentation.EditorIntent
import app.notekea.editor.presentation.SidePanelHost
import app.notekea.shell.domain.Workspace
import app.notekea.tree.presentation.SidebarRailActions

/** The rail's actions: search, the tools, the side panel, another vault, and About or Settings, with [onDialog]. */
internal fun railActionsOf(
    viewModels: WorkspaceViewModels,
    sidePanel: SidePanelHost,
    onSearch: () -> Unit,
    onSwitchVault: () -> Unit,
    onDialog: (WindowDialog) -> Unit
) = SidebarRailActions(
    onSearch = onSearch,
    onGraph = { openGraph(viewModels = viewModels) },
    onQuery = { viewModels.workspace.onIntent(intent = WorkspaceIntent.OnOpen(path = Workspace.QUERY)) },
    onSidePanelToggle = { sidePanel.onIntent(intent = EditorIntent.OnSidePanelToggle) },
    onSwitchVault = onSwitchVault,
    onAbout = { onDialog(WindowDialog.ABOUT) },
    onSettings = { onDialog(WindowDialog.SETTINGS) }
)
