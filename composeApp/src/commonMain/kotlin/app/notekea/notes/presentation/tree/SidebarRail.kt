package app.notekea.notes.presentation.tree

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.platform.domain.Shortcut
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.components.NotekeaLogo
import app.notekea.core.presentation.components.NotekeaToolRail
import app.notekea.core.presentation.components.NotekeaToolRailItem
import app.notekea.core.presentation.platform.label
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.resources.Res
import app.notekea.resources.rail_about
import app.notekea.resources.rail_label
import app.notekea.resources.rail_settings
import app.notekea.resources.rail_side_panel
import app.notekea.resources.tree_files
import app.notekea.resources.tree_search
import app.notekea.resources.tree_switch_vault
import app.notekea.resources.tree_today
import org.jetbrains.compose.resources.stringResource

private const val LOGO_SIZE_DP = 24

/**
 * The vault's tool rail on the window's left edge (`ROADMAP.md` 1.11), there whether the sidebar is shown or not, as in
 * Android Studio: the two panels first, Files (the sidebar, selected while it is shown) and the side panel (selected
 * while [isSidePanelVisible]), then Search notes and Today; at the bottom, as in Obsidian, Open another vault, About and Settings.
 * Each has a tooltip naming it and its shortcut; Notekea's logo tops the rail.
 */
@Composable
internal fun SidebarRail(
    state: VaultTreeState,
    onIntent: (VaultTreeIntent) -> Unit,
    actions: SidebarRailActions,
    isSidePanelVisible: Boolean,
    modifier: Modifier = Modifier
) {
    NotekeaToolRail(
        items = topItems(
            state = state,
            onIntent = onIntent,
            actions = actions,
            isSidePanelVisible = isSidePanelVisible
        ),
        label = stringResource(resource = Res.string.rail_label),
        modifier = modifier,
        bottomItems = bottomItems(actions = actions),
        header = { NotekeaLogo(size = LOGO_SIZE_DP.dp) }
    )
}

/** Files and the side panel, then Search notes and Today, with their shortcuts. */
@Composable
private fun topItems(
    state: VaultTreeState,
    onIntent: (VaultTreeIntent) -> Unit,
    actions: SidebarRailActions,
    isSidePanelVisible: Boolean
): List<NotekeaToolRailItem> {
    val conventions = state.platformConventions
    val shortcut = { it: Shortcut -> it.label(operatingSystem = conventions.operatingSystem) }
    return listOf(
        NotekeaToolRailItem(
            icon = NotekeaIcons.Files,
            label = stringResource(resource = Res.string.tree_files),
            onClick = { onIntent(VaultTreeIntent.OnSidebarToggle) },
            shortcutHint = shortcut(conventions.toggleSidebarShortcut),
            isSelected = !state.isSidebarCollapsed
        ),
        NotekeaToolRailItem(
            icon = NotekeaIcons.SidePanel,
            label = stringResource(resource = Res.string.rail_side_panel),
            onClick = actions.onSidePanelToggle,
            shortcutHint = shortcut(conventions.toggleSidePanelShortcut),
            isSelected = isSidePanelVisible
        ),
        NotekeaToolRailItem(
            icon = NotekeaIcons.Find,
            label = stringResource(resource = Res.string.tree_search),
            onClick = actions.onSearch,
            shortcutHint = shortcut(conventions.quickSwitcherShortcut)
        ),
        NotekeaToolRailItem(
            icon = NotekeaIcons.Today,
            label = stringResource(resource = Res.string.tree_today),
            onClick = { onIntent(VaultTreeIntent.OnTodayClick) },
            shortcutHint = shortcut(conventions.todayShortcut)
        )
    )
}

/** Open another vault, About and Settings. */
@Composable
private fun bottomItems(
    actions: SidebarRailActions
): List<NotekeaToolRailItem> = listOf(
    NotekeaToolRailItem(
        icon = NotekeaIcons.SwitchVault,
        label = stringResource(resource = Res.string.tree_switch_vault),
        onClick = actions.onSwitchVault
    ),
    NotekeaToolRailItem(
        icon = NotekeaIcons.About,
        label = stringResource(resource = Res.string.rail_about),
        onClick = actions.onAbout
    ),
    NotekeaToolRailItem(
        icon = NotekeaIcons.Settings,
        label = stringResource(resource = Res.string.rail_settings),
        onClick = actions.onSettings
    )
)

@Preview
@Composable
private fun SidebarRailPreview() {
    NotekeaPreviewThemes {
        SidebarRail(
            state = TreePreviewData.state(),
            onIntent = {},
            actions = SidebarRailActions(),
            isSidePanelVisible = true
        )
    }
}

@Preview
@Composable
private fun SidebarRailCollapsedPreview() {
    NotekeaPreviewThemes {
        SidebarRail(
            state = TreePreviewData.state().copy(isSidebarCollapsed = true),
            onIntent = {},
            actions = SidebarRailActions(),
            isSidePanelVisible = false
        )
    }
}
