package app.notekea.notes.presentation.tree

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.platform.domain.DialogButtonOrder
import app.notekea.core.presentation.components.NotekeaConfirmDialog
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.resources.Res
import app.notekea.resources.tree_links_dont_update
import app.notekea.resources.tree_links_message
import app.notekea.resources.tree_links_title
import app.notekea.resources.tree_links_update
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

private const val PREVIEW_NEW_NAME = "Arepas reina"
private const val PREVIEW_NOTE_COUNT = 4
private const val PREVIEW_OLD_NAME = "Arepas"

/**
 * Asks, after a rename, whether to update the links in the notes that point to the renamed entry: Notekea changes a
 * note only on an explicit answer. Saying no keeps the old links, as they were written.
 */
@Composable
internal fun LinkUpdateDialog(
    prompt: LinkUpdatePrompt,
    buttonOrder: DialogButtonOrder,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    NotekeaConfirmDialog(
        title = stringResource(resource = Res.string.tree_links_title),
        message = pluralStringResource(
            Res.plurals.tree_links_message,
            prompt.noteCount,
            prompt.noteCount,
            prompt.oldName,
            prompt.newName
        ),
        confirmLabel = stringResource(resource = Res.string.tree_links_update),
        cancelLabel = stringResource(resource = Res.string.tree_links_dont_update),
        buttonOrder = buttonOrder,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Preview
@Composable
private fun LinkUpdateDialogPreview() {
    NotekeaPreviewThemes {
        LinkUpdateDialog(
            prompt = LinkUpdatePrompt(
                oldName = PREVIEW_OLD_NAME,
                newName = PREVIEW_NEW_NAME,
                noteCount = PREVIEW_NOTE_COUNT
            ),
            buttonOrder = DialogButtonOrder.CANCEL_THEN_PRIMARY,
            onConfirm = {},
            onDismiss = {}
        )
    }
}
