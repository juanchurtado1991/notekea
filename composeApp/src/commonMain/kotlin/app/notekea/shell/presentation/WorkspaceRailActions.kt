package app.notekea.shell.presentation

import app.notekea.core.presentation.platform.WindowDialog
import app.notekea.editor.presentation.EditorIntent
import app.notekea.editor.presentation.SidePanelHost
import app.notekea.tree.presentation.SidebarRailActions

/** The rail's actions: search, the side panel, another vault, and About or Settings opened with [onDialog]. */
internal fun railActionsOf(
    sidePanel: SidePanelHost,
    onSearch: () -> Unit,
    onGraph: () -> Unit,
    onQuery: () -> Unit,
    onSwitchVault: () -> Unit,
    onDialog: (WindowDialog) -> Unit
) = SidebarRailActions(
    onSearch = onSearch,
    onGraph = onGraph,
    onQuery = onQuery,
    onSidePanelToggle = { sidePanel.onIntent(intent = EditorIntent.OnSidePanelToggle) },
    onSwitchVault = onSwitchVault,
    onAbout = { onDialog(WindowDialog.ABOUT) },
    onSettings = { onDialog(WindowDialog.SETTINGS) }
)
