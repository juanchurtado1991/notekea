package app.notekea.tree.presentation.view

import androidx.compose.runtime.Immutable

/**
 * What the tool rail's buttons beyond the tree do: Search notes ([onSearch]), the graph ([onGraph]), queries
 * ([onQuery]), tasks ([onTasks]), study cards ([onStudy]), the side panel ([onSidePanelToggle]), and at the bottom
 * another vault ([onSwitchVault]), About ([onAbout]) and Settings ([onSettings]); each does nothing unless given.
 */
@Immutable
data class SidebarRailActions(
    val onSearch: () -> Unit = {},
    val onGraph: () -> Unit = {},
    val onQuery: () -> Unit = {},
    val onTasks: () -> Unit = {},
    val onStudy: () -> Unit = {},
    val onSidePanelToggle: () -> Unit = {},
    val onSwitchVault: () -> Unit = {},
    val onAbout: () -> Unit = {},
    val onSettings: () -> Unit = {}
)
