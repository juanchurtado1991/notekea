package app.notekea.notes.presentation.tree

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.components.NotekeaIconButton
import app.notekea.core.presentation.components.NotekeaTooltip
import app.notekea.core.presentation.components.NotekeaMenuButton
import app.notekea.core.presentation.components.NotekeaMenuItem
import app.notekea.core.presentation.platform.displayPath
import app.notekea.core.presentation.platform.label
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.preview.PreviewPlatformConventions
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.notes.domain.TreeSort
import app.notekea.resources.Res
import app.notekea.resources.tree_collapse_all
import app.notekea.resources.tree_expand_all
import app.notekea.resources.tree_minimize
import app.notekea.resources.tree_sort_by_date
import app.notekea.resources.tree_sort_by_name
import app.notekea.resources.tree_sorted_by_date
import app.notekea.resources.tree_sorted_by_name
import org.jetbrains.compose.resources.stringResource

private const val PREVIEW_WIDTH_DP = 260

/**
 * Top of the sidebar, as in an IDE's project panel: the vault's name (its folder in the tooltip), then the actions on
 * the tree, each
 * with a tooltip naming it (and its shortcut): the sort (its icon shows the current one, its menu both choices),
 * expand all and collapse all folders, and minimize, which hides the sidebar and leaves its tool rail.
 */
@Composable
internal fun SidebarHeader(
    vaultName: String,
    vaultPath: String,
    sort: TreeSort,
    platformConventions: PlatformConventions,
    actions: SidebarHeaderActions
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(
            start = NotekeaDimensions.SpacingM,
            end = NotekeaDimensions.SpacingXs,
            top = NotekeaDimensions.SpacingS
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        VaultName(
            vaultName = vaultName,
            vaultPath = vaultPath,
            platformConventions = platformConventions,
            modifier = Modifier.weight(weight = 1f)
        )
        SortButton(sort = sort, onSortChange = actions.onSortChange)
        NotekeaIconButton(
            icon = NotekeaIcons.ExpandAll,
            label = stringResource(resource = Res.string.tree_expand_all),
            onClick = actions.onExpandAll
        )
        NotekeaIconButton(
            icon = NotekeaIcons.CollapseAll,
            label = stringResource(resource = Res.string.tree_collapse_all),
            onClick = actions.onCollapseAll
        )
        NotekeaIconButton(
            icon = NotekeaIcons.Minimize,
            label = stringResource(resource = Res.string.tree_minimize),
            onClick = actions.onMinimize,
            shortcutHint = platformConventions.toggleSidebarShortcut.label(
                operatingSystem = platformConventions.operatingSystem
            )
        )
    }
}

/**
 * The vault's name, cut with an ellipsis when long; its folder shows in the tooltip, since the status bar already says
 * which vault is open.
 */
@Composable
private fun VaultName(
    vaultName: String,
    vaultPath: String,
    platformConventions: PlatformConventions,
    modifier: Modifier
) {
    Box(modifier = modifier) {
        NotekeaTooltip(label = displayPath(path = vaultPath, platformConventions = platformConventions)) {
            Text(
                text = vaultName,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** Shows the current sort (icon and tooltip) and opens the choices, the current one checked. */
@Composable
private fun SortButton(
    sort: TreeSort,
    onSortChange: (TreeSort) -> Unit
) {
    val choices = listOf(TreeSort.NAME, TreeSort.MODIFIED).map { choice ->
        NotekeaMenuItem(
            label = stringResource(
                resource = if (choice == TreeSort.NAME) Res.string.tree_sort_by_name else Res.string.tree_sort_by_date
            ),
            onClick = { onSortChange(choice) },
            isChecked = choice == sort
        )
    }
    NotekeaMenuButton(
        icon = if (sort == TreeSort.NAME) NotekeaIcons.SortByName else NotekeaIcons.SortByModified,
        label = stringResource(
            resource = if (sort == TreeSort.NAME) Res.string.tree_sorted_by_name else Res.string.tree_sorted_by_date
        ),
        sections = listOf(choices)
    )
}

@Preview
@Composable
private fun SidebarHeaderPreview() {
    NotekeaPreviewThemes {
        Column(modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)) {
            SidebarHeader(
                vaultName = TreePreviewData.VAULT_NAME,
                vaultPath = TreePreviewData.VAULT_PATH,
                sort = TreeSort.NAME,
                platformConventions = PreviewPlatformConventions.linux,
                actions = SidebarHeaderActions(onSortChange = {}, onExpandAll = {}, onCollapseAll = {}, onMinimize = {})
            )
        }
    }
}
