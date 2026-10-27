package app.notekea.notes.presentation.tree

import androidx.compose.runtime.Immutable

/**
 * What the tool rail's buttons beyond the tree do: Search notes ([onSearch]), show or hide the side panel
 * ([onSidePanelToggle]), and at the bottom Open another vault ([onSwitchVault]), About ([onAbout]) and Settings
 * ([onSettings]). Each does nothing unless given, as in previews and tests.
 */
@Immutable
data class SidebarRailActions(
    val onSearch: () -> Unit = {},
    val onSidePanelToggle: () -> Unit = {},
    val onSwitchVault: () -> Unit = {},
    val onAbout: () -> Unit = {},
    val onSettings: () -> Unit = {}
)
