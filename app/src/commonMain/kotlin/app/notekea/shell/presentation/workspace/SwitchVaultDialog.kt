package app.notekea.shell.presentation.workspace

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.platform.domain.DialogButtonOrder
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.platform.domain.stacksAlertButtons
import app.notekea.core.presentation.designsystem.components.NotekeaConfirmDialog
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.resources.Res
import app.notekea.resources.switch_vault_confirm
import app.notekea.resources.switch_vault_message
import app.notekea.resources.switch_vault_title
import app.notekea.resources.tree_cancel
import org.jetbrains.compose.resources.stringResource

/**
 * "Open another vault?" before the rail's button leaves the vault named [vaultName] for the screen that picks one (the
 * developer's request of 2026-11-13: a click there changed the whole window at once). Nothing is lost either way.
 */
@Composable
internal fun SwitchVaultDialog(
    vaultName: String,
    buttonOrder: DialogButtonOrder,
    isStacked: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    NotekeaConfirmDialog(
        title = stringResource(resource = Res.string.switch_vault_title),
        message = stringResource(Res.string.switch_vault_message, vaultName),
        confirmLabel = stringResource(resource = Res.string.switch_vault_confirm),
        cancelLabel = stringResource(resource = Res.string.tree_cancel),
        buttonOrder = buttonOrder,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        isStacked = isStacked
    )
}

/**
 * What the rail's Open another vault does: it asks first ([SwitchVaultDialog]), since it leaves the whole vault for
 * another screen, and only its confirm calls [onSwitchVault].
 */
@Composable
internal fun switchVaultAsked(
    vaultName: String,
    conventions: PlatformConventions,
    onSwitchVault: () -> Unit
): () -> Unit {
    var isAsked by remember { mutableStateOf(value = false) }

    if (isAsked) {
        SwitchVaultDialog(
            vaultName = vaultName,
            buttonOrder = conventions.dialogButtonOrder,
            isStacked = conventions.stacksAlertButtons,
            onConfirm = onSwitchVault,
            onDismiss = { isAsked = false }
        )
    }

    return { isAsked = true }
}

@Preview
@Composable
private fun SwitchVaultDialogPreview() {
    NotekeaPreviewThemes {
        SwitchVaultDialog(
            vaultName = "Playground Vault",
            buttonOrder = DialogButtonOrder.CANCEL_THEN_PRIMARY,
            isStacked = false,
            onConfirm = {},
            onDismiss = {}
        )
    }
}
