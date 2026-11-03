package app.notekea.notes.presentation.tree

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.platform.domain.DialogButtonOrder
import app.notekea.core.presentation.components.NotekeaConfirmDialog
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.notes.domain.NoteRewrite
import app.notekea.notes.domain.RelativePath
import app.notekea.resources.Res
import app.notekea.resources.tree_link_update_count_links
import app.notekea.resources.tree_link_update_count_notes
import app.notekea.resources.tree_link_update_decline
import app.notekea.resources.tree_link_update_message
import app.notekea.resources.tree_link_update_title
import app.notekea.resources.tree_link_update_confirm
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Asks after a rename whether the links to the entry follow its new name (`ROADMAP.md` 2.5): "Update 12 links in 7
 * notes?". Update links writes them; Don't update (or Esc) keeps them as they are, pointing to the old name.
 */
@Composable
internal fun LinkUpdateDialog(
    offer: LinkUpdateOffer,
    buttonOrder: DialogButtonOrder,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val links = pluralStringResource(Res.plurals.tree_link_update_count_links, offer.linkCount, offer.linkCount)
    val notes = pluralStringResource(Res.plurals.tree_link_update_count_notes, offer.noteCount, offer.noteCount)
    NotekeaConfirmDialog(
        title = stringResource(Res.string.tree_link_update_title, links, notes),
        message = stringResource(
            Res.string.tree_link_update_message,
            offer.from.nameWithoutExtension,
            offer.to.nameWithoutExtension
        ),
        confirmLabel = stringResource(resource = Res.string.tree_link_update_confirm),
        cancelLabel = stringResource(resource = Res.string.tree_link_update_decline),
        buttonOrder = buttonOrder,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Preview
@Composable
private fun LinkUpdateDialogPreview() {
    val rewrite = NoteRewrite(path = RelativePath(value = "Welcome.md"), original = "", updated = "", linkCount = 2)
    NotekeaPreviewThemes {
        LinkUpdateDialog(
            offer = LinkUpdateOffer(
                from = RelativePath(value = "Recipes/Arepas.md"),
                to = RelativePath(value = "Recipes/Arepas reina.md"),
                rewrites = listOf(rewrite, rewrite.copy(path = RelativePath(value = "Recipes/Weekly menu.md")))
            ),
            buttonOrder = DialogButtonOrder.CANCEL_THEN_PRIMARY,
            onConfirm = {},
            onDismiss = {}
        )
    }
}
