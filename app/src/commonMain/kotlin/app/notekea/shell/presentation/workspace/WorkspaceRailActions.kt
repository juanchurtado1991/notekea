package app.notekea.shell.presentation.workspace

import app.notekea.core.presentation.platform.WindowDialog
import app.notekea.editor.presentation.model.EditorIntent
import app.notekea.editor.presentation.sidepanel.SidePanelHost
import app.notekea.shell.domain.Workspace
import app.notekea.shell.presentation.tabs.openGraph
import app.notekea.tree.presentation.view.SidebarRailActions

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
    onQuery = {
        viewModels.workspace.onIntent(intent = WorkspaceIntent.OnOpen(path = Workspace.QUERY))
    },
    onTasks = {
        viewModels.workspace.onIntent(intent = WorkspaceIntent.OnOpen(path = Workspace.TASKS))
    },
    onStudy = {
        viewModels.workspace.onIntent(intent = WorkspaceIntent.OnOpen(path = Workspace.STUDY))
    },
    onSidePanelToggle = { sidePanel.onIntent(intent = EditorIntent.OnSidePanelToggle) },
    onSwitchVault = onSwitchVault,
    onSettings = { onDialog(WindowDialog.SETTINGS) }
)
