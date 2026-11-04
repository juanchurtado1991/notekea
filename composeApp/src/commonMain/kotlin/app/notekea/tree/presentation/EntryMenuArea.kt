package app.notekea.tree.presentation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.components.NotekeaContextMenuArea
import app.notekea.core.presentation.components.NotekeaContextMenuState
import app.notekea.core.presentation.components.NotekeaMenuItem
import app.notekea.core.presentation.platform.label
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.tree.domain.FolderCard
import app.notekea.tree.domain.TreeRow
import app.notekea.resources.Res
import app.notekea.resources.tree_bookmark_add
import app.notekea.resources.tree_bookmark_remove
import app.notekea.resources.tree_delete_file
import app.notekea.resources.tree_delete_folder
import app.notekea.resources.tree_delete_note
import app.notekea.resources.tree_rename
import org.jetbrains.compose.resources.stringResource

/**
 * [content] with the menu of a vault entry, in the tree or a folder's grid: [createItems] when there are any, then
 * bookmark, rename and show in the file manager, then delete. It is built, from the latest [state], only while open.
 */
@Composable
internal fun EntryMenuArea(
    row: TreeRow,
    state: () -> VaultTreeState,
    onIntent: (VaultTreeIntent) -> Unit,
    createItems: @Composable () -> List<NotekeaMenuItem> = { emptyList() },
    menuState: NotekeaContextMenuState = remember { NotekeaContextMenuState() },
    onOpenByPointer: () -> Unit = {},
    content: @Composable () -> Unit
) {
    NotekeaContextMenuArea(
        menu = { menuSections(row = row, state = state(), createItems = createItems(), onIntent = onIntent) },
        state = menuState,
        onOpenByPointer = onOpenByPointer,
        content = content
    )
}

/** The row a folder's card stands for, in what its menu says and does. */
internal fun FolderCard.asTreeRow(): TreeRow = when (kind) {
    FolderCard.Kind.FOLDER -> TreeRow.Folder(path = path, depth = 0, fileCount = 0, isExpanded = false)
    FolderCard.Kind.NOTE -> TreeRow.Note(path = path, depth = 0, modifiedAt = null)
    FolderCard.Kind.IMAGE, FolderCard.Kind.FILE -> TreeRow.File(path = path, depth = 0, modifiedAt = null)
}

@Composable
private fun menuSections(
    row: TreeRow,
    state: VaultTreeState,
    createItems: List<NotekeaMenuItem>,
    onIntent: (VaultTreeIntent) -> Unit
): List<List<NotekeaMenuItem>> = listOf(
    createItems,
    editSection(row = row, state = state, onIntent = onIntent),
    listOf(
        NotekeaMenuItem(
            label = stringResource(
                resource = when (row) {
                    is TreeRow.Folder -> Res.string.tree_delete_folder
                    is TreeRow.Note -> Res.string.tree_delete_note
                    is TreeRow.File -> Res.string.tree_delete_file
                }
            ),
            onClick = { onIntent(VaultTreeIntent.OnDeleteRequest(path = row.path)) },
            icon = NotekeaIcons.Delete,
            shortcutHint = state.platformConventions.deleteShortcut.label(
                operatingSystem = state.platformConventions.operatingSystem
            ),
            isDestructive = true
        )
    )
).filter { it.isNotEmpty() }

@Composable
private fun editSection(
    row: TreeRow,
    state: VaultTreeState,
    onIntent: (VaultTreeIntent) -> Unit
): List<NotekeaMenuItem> {
    val os = state.platformConventions.operatingSystem
    val isBookmarked = row.path in state.bookmarks
    return listOf(
        NotekeaMenuItem(
            label = stringResource(
                resource = if (isBookmarked) Res.string.tree_bookmark_remove else Res.string.tree_bookmark_add
            ),
            onClick = { onIntent(BookmarkIntent.OnToggle(path = row.path)) },
            icon = NotekeaIcons.Bookmark,
            shortcutHint = state.platformConventions.bookmarkShortcut.label(operatingSystem = os)
        ),
        NotekeaMenuItem(
            label = stringResource(resource = Res.string.tree_rename),
            onClick = { onIntent(VaultTreeIntent.OnRenameRequest(path = row.path)) },
            icon = NotekeaIcons.Rename,
            shortcutHint = state.platformConventions.renameShortcut.label(operatingSystem = os)
        ),
        NotekeaMenuItem(
            label = stringResource(resource = revealLabel(operatingSystem = os)),
            onClick = { onIntent(VaultTreeIntent.OnRevealRequest(path = row.path)) },
            icon = NotekeaIcons.Reveal
        )
    )
}

@Preview
@Composable
private fun EntryMenuAreaPreview() {
    val state = TreePreviewData.state()
    NotekeaPreviewThemes {
        EntryMenuArea(row = state.rows.first(), state = { state }, onIntent = {}) {
            Text(text = TreePreviewData.VAULT_NAME)
        }
    }
}
