package app.notekea.tree.presentation.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.platform.domain.Shortcut
import app.notekea.core.platform.domain.graphShortcut
import app.notekea.core.presentation.designsystem.components.NotekeaLogo
import app.notekea.core.presentation.designsystem.components.layout.NotekeaToolRail
import app.notekea.core.presentation.designsystem.components.layout.NotekeaToolRailItem
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons
import app.notekea.core.presentation.platform.label
import app.notekea.resources.Res
import app.notekea.resources.rail_graph
import app.notekea.resources.rail_label
import app.notekea.resources.rail_query
import app.notekea.resources.rail_settings
import app.notekea.resources.rail_side_panel
import app.notekea.resources.rail_study
import app.notekea.resources.rail_tasks
import app.notekea.resources.tree_files
import app.notekea.resources.tree_search
import app.notekea.resources.tree_switch_vault
import app.notekea.resources.tree_today
import app.notekea.tree.presentation.model.TreePreviewData
import app.notekea.tree.presentation.model.VaultTreeIntent
import app.notekea.tree.presentation.model.VaultTreeState
import org.jetbrains.compose.resources.stringResource

private const val LOGO_SIZE_DP = 24

/**
 * The vault's tool rail on the window's left edge, shown or not the sidebar: Search notes, Files and the side panel
 * (each selected while shown), under a line the views, Today first, and at the bottom Open another vault and Settings,
 * which holds About. Notekea's logo tops the rail.
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
        items = panelItems(
            state = state,
            onIntent = onIntent,
            actions = actions,
            isSidePanelVisible = isSidePanelVisible
        ),
        toolItems = toolItems(
            state = state,
            onIntent = onIntent,
            actions = actions
        ),
        label = stringResource(resource = Res.string.rail_label),
        modifier = modifier,
        bottomItems = bottomItems(actions = actions),
        header = { NotekeaLogo(size = LOGO_SIZE_DP.dp) }
    )
}

/** Search notes, Files and the side panel, with their shortcuts. */
@Composable
private fun panelItems(
    state: VaultTreeState,
    onIntent: (VaultTreeIntent) -> Unit,
    actions: SidebarRailActions,
    isSidePanelVisible: Boolean
): List<NotekeaToolRailItem> {
    val conventions = state.platformConventions

    val shortcut = { it: Shortcut -> it.label(operatingSystem = conventions.operatingSystem) }

    return listOf(
        NotekeaToolRailItem(
            icon = NotekeaIcons.Find,
            label = stringResource(resource = Res.string.tree_search),
            onClick = actions.onSearch,
            shortcutHint = shortcut(conventions.quickSwitcherShortcut)
        ),
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
        )
    )
}

/** The views: Today, tasks, the graph, queries and study cards, with their shortcuts. */
@Composable
private fun toolItems(
    state: VaultTreeState,
    onIntent: (VaultTreeIntent) -> Unit,
    actions: SidebarRailActions
): List<NotekeaToolRailItem> {
    val conventions = state.platformConventions

    val shortcut = { it: Shortcut -> it.label(operatingSystem = conventions.operatingSystem) }

    return listOf(
        NotekeaToolRailItem(
            icon = NotekeaIcons.Today,
            label = stringResource(resource = Res.string.tree_today),
            onClick = { onIntent(VaultTreeIntent.OnTodayClick) },
            shortcutHint = shortcut(conventions.todayShortcut)
        ),
        NotekeaToolRailItem(
            icon = NotekeaIcons.Tasks,
            label = stringResource(resource = Res.string.rail_tasks),
            onClick = actions.onTasks
        ),
        NotekeaToolRailItem(
            icon = NotekeaIcons.Graph,
            label = stringResource(resource = Res.string.rail_graph),
            onClick = actions.onGraph,
            shortcutHint = shortcut(conventions.graphShortcut)
        ),
        NotekeaToolRailItem(
            icon = NotekeaIcons.Query,
            label = stringResource(resource = Res.string.rail_query),
            onClick = actions.onQuery
        ),
        NotekeaToolRailItem(
            icon = NotekeaIcons.Study,
            label = stringResource(resource = Res.string.rail_study),
            onClick = actions.onStudy
        )
    )
}

/** Open another vault and Settings. */
@Composable
private fun bottomItems(actions: SidebarRailActions): List<NotekeaToolRailItem> = listOf(
    NotekeaToolRailItem(
        icon = NotekeaIcons.SwitchVault,
        label = stringResource(resource = Res.string.tree_switch_vault),
        onClick = actions.onSwitchVault
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
