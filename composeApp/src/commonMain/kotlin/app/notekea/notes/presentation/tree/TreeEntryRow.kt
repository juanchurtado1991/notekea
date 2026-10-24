package app.notekea.notes.presentation.tree

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.platform.domain.OperatingSystem
import app.notekea.core.presentation.components.NotekeaContextMenuArea
import app.notekea.core.presentation.components.NotekeaContextMenuState
import app.notekea.core.presentation.components.NotekeaInlineTextField
import app.notekea.core.presentation.components.NotekeaMenuItem
import app.notekea.core.presentation.components.NotekeaTreeItem
import app.notekea.core.presentation.platform.label
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.TreeRow
import app.notekea.resources.Res
import app.notekea.resources.tree_delete_folder
import app.notekea.resources.tree_delete_note
import app.notekea.resources.tree_new_folder
import app.notekea.resources.tree_new_note
import app.notekea.resources.tree_open_folder
import app.notekea.resources.tree_rename
import app.notekea.resources.tree_reveal_explorer
import app.notekea.resources.tree_reveal_files
import app.notekea.resources.tree_reveal_finder
import app.notekea.resources.tree_select_note
import app.notekea.resources.tree_untitled_folder
import app.notekea.resources.tree_untitled_note
import org.jetbrains.compose.resources.stringResource

private const val PREVIEW_WIDTH_DP = 260

/**
 * One entry of the tree with its context menu (create here, rename, show in the file manager, delete) and, while it is
 * renamed, the name field with the reason a name was refused below it. A right click selects the entry first.
 */
@Composable
internal fun TreeEntryRow(
    row: TreeRow,
    state: VaultTreeState,
    isFocused: Boolean,
    isMenuRequested: Boolean,
    onMenuShown: () -> Unit,
    onIntent: (VaultTreeIntent) -> Unit,
    onFocusTree: () -> Unit
) {
    val menuState = remember { NotekeaContextMenuState() }
    LaunchedEffect(key1 = isMenuRequested) {
        if (isMenuRequested) {
            menuState.open()
            onMenuShown()
        }
    }
    NotekeaContextMenuArea(
        sections = menuSections(row = row, state = state, onIntent = onIntent),
        state = menuState,
        onOpenByPointer = { onIntent(VaultTreeIntent.OnRowSelect(path = row.path)) }
    ) {
        Column {
            EntryItem(row = row, state = state, isFocused = isFocused, onIntent = onIntent, onFocusTree = onFocusTree)
            val error = state.renaming?.takeIf { it.path == row.path }?.error
            error?.let {
                Text(
                    text = notesErrorMessage(error = it, platformConventions = state.platformConventions),
                    modifier = Modifier.padding(
                        start = NotekeaDimensions.TreeIndent * (row.depth + 1),
                        bottom = NotekeaDimensions.SpacingXs
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun EntryItem(
    row: TreeRow,
    state: VaultTreeState,
    isFocused: Boolean,
    onIntent: (VaultTreeIntent) -> Unit,
    onFocusTree: () -> Unit
) {
    val draft = state.renaming?.takeIf { it.path == row.path }
    val folder = row as? TreeRow.Folder
    NotekeaTreeItem(
        label = if (folder != null) row.path.name else row.path.nameWithoutExtension,
        icon = when {
            folder == null -> NotekeaIcons.Note
            folder.isExpanded -> NotekeaIcons.FolderOpen
            else -> NotekeaIcons.Folder
        },
        depth = row.depth,
        onClick = {
            onIntent(VaultTreeIntent.OnRowClick(path = row.path))
            onFocusTree()
        },
        selected = row.path == state.selectedPath,
        focused = isFocused && draft == null,
        isExpanded = folder?.isExpanded,
        onToggleExpanded = folder?.let { { onIntent(VaultTreeIntent.OnChevronClick(path = it.path)) } },
        trailingText = folder?.noteCount?.takeIf { it > 0 }?.toString(),
        onClickLabel = stringResource(
            resource = if (folder !=
                null
            ) {
                Res.string.tree_open_folder
            } else {
                Res.string.tree_select_note
            }
        ),
        labelContent = draft?.let { { RenameField(draft = it, onIntent = onIntent) } }
    )
}

@Composable
private fun RenameField(
    draft: RenameDraft,
    onIntent: (VaultTreeIntent) -> Unit
) {
    NotekeaInlineTextField(
        value = draft.text,
        onValueChange = { onIntent(VaultTreeIntent.OnRenameTextChange(text = it)) },
        onSubmit = { onIntent(VaultTreeIntent.OnRenameConfirm) },
        onCancel = { onIntent(VaultTreeIntent.OnRenameCancel) },
        isError = draft.error != null,
        onFocusLost = { onIntent(VaultTreeIntent.OnRenameConfirm) }
    )
}

@Composable
private fun menuSections(
    row: TreeRow,
    state: VaultTreeState,
    onIntent: (VaultTreeIntent) -> Unit
): List<List<NotekeaMenuItem>> = listOf(
    createSection(state = state, onIntent = onIntent),
    editSection(row = row, state = state, onIntent = onIntent),
    listOf(
        NotekeaMenuItem(
            label = stringResource(
                resource = if (row is TreeRow.Folder) Res.string.tree_delete_folder else Res.string.tree_delete_note
            ),
            onClick = { onIntent(VaultTreeIntent.OnDeleteRequest(path = row.path)) },
            icon = NotekeaIcons.Delete,
            shortcutHint = state.platformConventions.deleteShortcut.label(
                operatingSystem = state.platformConventions.operatingSystem
            ),
            isDestructive = true
        )
    )
)

@Composable
private fun createSection(
    state: VaultTreeState,
    onIntent: (VaultTreeIntent) -> Unit
): List<NotekeaMenuItem> {
    val untitledNote = stringResource(resource = Res.string.tree_untitled_note)
    val untitledFolder = stringResource(resource = Res.string.tree_untitled_folder)
    return listOf(
        NotekeaMenuItem(
            label = stringResource(resource = Res.string.tree_new_note),
            onClick = { onIntent(VaultTreeIntent.OnNewNoteClick(baseName = untitledNote)) },
            icon = NotekeaIcons.NewNote,
            shortcutHint = state.platformConventions.newNoteShortcut.label(
                operatingSystem = state.platformConventions.operatingSystem
            )
        ),
        NotekeaMenuItem(
            label = stringResource(resource = Res.string.tree_new_folder),
            onClick = { onIntent(VaultTreeIntent.OnNewFolderClick(baseName = untitledFolder)) },
            icon = NotekeaIcons.NewFolder
        )
    )
}

@Composable
private fun editSection(
    row: TreeRow,
    state: VaultTreeState,
    onIntent: (VaultTreeIntent) -> Unit
): List<NotekeaMenuItem> {
    val os = state.platformConventions.operatingSystem
    return listOf(
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

private fun revealLabel(
    operatingSystem: OperatingSystem
) = when (operatingSystem) {
    OperatingSystem.LINUX -> Res.string.tree_reveal_files
    OperatingSystem.MACOS -> Res.string.tree_reveal_finder
    OperatingSystem.WINDOWS -> Res.string.tree_reveal_explorer
}

@Preview
@Composable
private fun TreeEntryRowPreview() {
    val state = TreePreviewData.renamingState().let {
        it.copy(renaming = it.renaming?.copy(error = app.notekea.notes.domain.NotesError.HiddenName))
    }
    NotekeaPreviewThemes {
        Column(modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)) {
            state.rows.forEach { row ->
                TreeEntryRow(
                    row = row,
                    state = state,
                    isFocused = false,
                    isMenuRequested = false,
                    onMenuShown = {},
                    onIntent = {},
                    onFocusTree = {}
                )
            }
        }
    }
}
