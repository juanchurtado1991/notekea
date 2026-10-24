package app.notekea.notes.presentation.tree

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.platform.domain.DialogButtonOrder
import app.notekea.core.presentation.components.NotekeaConfirmDialog
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.VaultTree
import app.notekea.resources.Res
import app.notekea.resources.tree_cancel
import app.notekea.resources.tree_delete_folder
import app.notekea.resources.tree_delete_folder_message
import app.notekea.resources.tree_delete_folder_title
import app.notekea.resources.tree_delete_message
import app.notekea.resources.tree_delete_note
import app.notekea.resources.tree_delete_note_title
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Asks before moving [path] to the trash. A folder says how many notes go with it, so nobody deletes a folder full of
 * notes thinking it is empty; an empty folder reads like a single note.
 */
@Composable
internal fun DeleteEntryDialog(
    path: RelativePath,
    tree: VaultTree,
    buttonOrder: DialogButtonOrder,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val isFolder = path in tree.folders
    val noteCount = if (isFolder) tree.notes.keys.count { it.isWithin(folder = path) } else 0
    val name = path.nameWithoutExtension
    NotekeaConfirmDialog(
        title = stringResource(
            resource = if (isFolder) Res.string.tree_delete_folder_title else Res.string.tree_delete_note_title
        ),
        message = if (noteCount > 0) {
            pluralStringResource(Res.plurals.tree_delete_folder_message, noteCount, name, noteCount)
        } else {
            stringResource(Res.string.tree_delete_message, name)
        },
        confirmLabel = stringResource(
            resource = if (isFolder) Res.string.tree_delete_folder else Res.string.tree_delete_note
        ),
        cancelLabel = stringResource(resource = Res.string.tree_cancel),
        buttonOrder = buttonOrder,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        isDestructive = true
    )
}

@Preview
@Composable
private fun DeleteEntryDialogPreview() {
    NotekeaPreviewThemes {
        DeleteEntryDialog(
            path = RelativePath(value = "Projects"),
            tree = TreePreviewData.tree,
            buttonOrder = DialogButtonOrder.CANCEL_THEN_PRIMARY,
            onConfirm = {},
            onDismiss = {}
        )
    }
}
