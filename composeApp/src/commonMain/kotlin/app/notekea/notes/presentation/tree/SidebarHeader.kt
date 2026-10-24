package app.notekea.notes.presentation.tree

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import app.notekea.core.presentation.platform.displayPath
import app.notekea.core.presentation.platform.label
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.preview.PreviewPlatformConventions
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.notes.domain.TreeSort
import app.notekea.resources.Res
import app.notekea.resources.tree_hide_sidebar
import app.notekea.resources.tree_new_folder
import app.notekea.resources.tree_new_note
import app.notekea.resources.tree_sort_by_date
import app.notekea.resources.tree_sort_by_name
import org.jetbrains.compose.resources.stringResource

private const val PREVIEW_WIDTH_DP = 260

/**
 * Top of the sidebar: the vault's name and folder, and the actions on the tree. Every icon has a tooltip with its
 * name and shortcut. The sort button shows what clicking it does (sort by date when notes are sorted by name).
 */
@Composable
internal fun SidebarHeader(
    vaultName: String,
    vaultPath: String,
    sort: TreeSort,
    platformConventions: PlatformConventions,
    onNewNote: () -> Unit,
    onNewFolder: () -> Unit,
    onSortToggle: () -> Unit,
    onHideSidebar: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(
            start = NotekeaDimensions.SpacingM,
            end = NotekeaDimensions.SpacingS,
            top = NotekeaDimensions.SpacingM
        )
    ) {
        VaultTitle(
            vaultName = vaultName,
            vaultPath = vaultPath,
            platformConventions = platformConventions,
            onHideSidebar = onHideSidebar
        )
        TreeToolbar(
            sort = sort,
            platformConventions = platformConventions,
            onNewNote = onNewNote,
            onNewFolder = onNewFolder,
            onSortToggle = onSortToggle
        )
    }
}

@Composable
private fun VaultTitle(
    vaultName: String,
    vaultPath: String,
    platformConventions: PlatformConventions,
    onHideSidebar: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(
                text = vaultName,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = displayPath(path = vaultPath, platformConventions = platformConventions),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        NotekeaIconButton(
            icon = NotekeaIcons.HideSidebar,
            label = stringResource(resource = Res.string.tree_hide_sidebar),
            onClick = onHideSidebar,
            shortcutHint = platformConventions.toggleSidebarShortcut.label(
                operatingSystem = platformConventions.operatingSystem
            )
        )
    }
}

@Composable
private fun TreeToolbar(
    sort: TreeSort,
    platformConventions: PlatformConventions,
    onNewNote: () -> Unit,
    onNewFolder: () -> Unit,
    onSortToggle: () -> Unit
) {
    Row(
        modifier = Modifier.padding(top = NotekeaDimensions.SpacingS),
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)
    ) {
        NotekeaIconButton(
            icon = NotekeaIcons.NewNote,
            label = stringResource(resource = Res.string.tree_new_note),
            onClick = onNewNote,
            shortcutHint = platformConventions.newNoteShortcut.label(
                operatingSystem = platformConventions.operatingSystem
            )
        )
        NotekeaIconButton(
            icon = NotekeaIcons.NewFolder,
            label = stringResource(resource = Res.string.tree_new_folder),
            onClick = onNewFolder
        )
        Spacer(modifier = Modifier.weight(weight = 1f))
        SortButton(sort = sort, onSortToggle = onSortToggle)
    }
}

@Composable
private fun SortButton(
    sort: TreeSort,
    onSortToggle: () -> Unit
) {
    when (sort) {
        TreeSort.NAME -> NotekeaIconButton(
            icon = NotekeaIcons.SortByModified,
            label = stringResource(resource = Res.string.tree_sort_by_date),
            onClick = onSortToggle
        )

        TreeSort.MODIFIED -> NotekeaIconButton(
            icon = NotekeaIcons.SortByName,
            label = stringResource(resource = Res.string.tree_sort_by_name),
            onClick = onSortToggle
        )
    }
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
                onNewNote = {},
                onNewFolder = {},
                onSortToggle = {},
                onHideSidebar = {}
            )
        }
    }
}
