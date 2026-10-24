package app.notekea.vault.presentation.welcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import app.notekea.core.presentation.components.NotekeaProgressIndicator
import app.notekea.resources.welcome_busy
import app.notekea.vault.domain.VaultError
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.platform.domain.DialogButtonOrder
import app.notekea.core.platform.domain.OperatingSystem
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.components.NotekeaButton
import app.notekea.core.presentation.components.NotekeaButtonStyle
import app.notekea.core.presentation.components.NotekeaCard
import app.notekea.core.presentation.components.NotekeaTextField
import app.notekea.core.presentation.platform.displayPath
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.resources.Res
import app.notekea.resources.welcome_new_vault_cancel
import app.notekea.resources.welcome_new_vault_change_location
import app.notekea.resources.welcome_new_vault_confirm
import app.notekea.resources.welcome_new_vault_default_name
import app.notekea.resources.welcome_new_vault_location_label
import app.notekea.resources.welcome_new_vault_name_label
import app.notekea.resources.welcome_new_vault_summary
import app.notekea.resources.welcome_new_vault_title
import org.jetbrains.compose.resources.stringResource

private const val PREVIEW_INVALID_NAME = "a/b"
private const val PREVIEW_WIDTH_DP = 520
private const val UNIX_SEPARATOR = "/"
private const val WINDOWS_SEPARATOR = "\\"

/**
 * Inline "Create a new vault" panel: a suggested name and location, so a first-time user only has to press
 * "Create vault", and a sentence saying exactly which folder will be created. The name field takes focus when the
 * panel opens, Enter creates and Esc cancels; errors appear under the field they are about, keeping what was typed.
 * Buttons follow the platform order.
 */
@Composable
internal fun NewVaultPanel(
    form: NewVaultForm,
    platformConventions: PlatformConventions,
    onNameChange: (String) -> Unit,
    onChangeLocation: () -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    error: VaultError?,
    isBusy: Boolean = false
) {
    val nameFocus = remember { FocusRequester() }
    LaunchedEffect(key1 = Unit) { nameFocus.requestFocus() }
    NotekeaCard(modifier = Modifier.fillMaxWidth().cancelOnEscape(onCancel = onCancel)) {
        Column(
            modifier = Modifier.padding(all = NotekeaDimensions.SpacingL),
            verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingM)
        ) {
            Text(
                text = stringResource(resource = Res.string.welcome_new_vault_title),
                style = MaterialTheme.typography.titleMedium
            )
            NameField(
                name = form.name,
                platformConventions = platformConventions,
                focusRequester = nameFocus,
                onNameChange = onNameChange,
                error = error?.takeIf { it.isAboutName() },
                onSubmit = onConfirm.takeIf { !isBusy }
            )
            FieldLabel(text = stringResource(resource = Res.string.welcome_new_vault_location_label))
            LocationRow(
                form = form,
                platformConventions = platformConventions,
                onChangeLocation = onChangeLocation,
                enabled = !isBusy,
                error = error?.takeUnless { it.isAboutName() }
            )
            PanelButtons(
                dialogButtonOrder = platformConventions.dialogButtonOrder,
                onCancel = onCancel,
                onConfirm = onConfirm,
                isBusy = isBusy
            )
        }
    }
}

@Composable
private fun FieldLabel(
    text: String
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun LocationRow(
    form: NewVaultForm,
    platformConventions: PlatformConventions,
    onChangeLocation: () -> Unit,
    enabled: Boolean,
    error: VaultError?
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = displayPath(path = form.parentPath, platformConventions = platformConventions),
            modifier = Modifier.weight(weight = 1f),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        NotekeaButton(
            text = stringResource(resource = Res.string.welcome_new_vault_change_location),
            onClick = onChangeLocation,
            style = NotekeaButtonStyle.SECONDARY,
            enabled = enabled
        )
    }
    Text(
        text = error?.let { vaultErrorMessage(error = it, platformConventions = platformConventions) }
            ?: stringResource(Res.string.welcome_new_vault_summary, newFolderPath(form, platformConventions)),
        style = MaterialTheme.typography.bodySmall,
        color = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun NameField(
    name: String,
    platformConventions: PlatformConventions,
    focusRequester: FocusRequester,
    onNameChange: (String) -> Unit,
    error: VaultError?,
    onSubmit: (() -> Unit)?
) {
    FieldLabel(text = stringResource(resource = Res.string.welcome_new_vault_name_label))
    NotekeaTextField(
        value = name,
        onValueChange = onNameChange,
        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester = focusRequester),
        placeholder = stringResource(resource = Res.string.welcome_new_vault_default_name),
        errorText = error?.let { vaultErrorMessage(error = it, platformConventions = platformConventions) },
        onSubmit = onSubmit
    )
}

@Composable
private fun PanelButtons(
    dialogButtonOrder: DialogButtonOrder,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    isBusy: Boolean
) {
    val cancel: @Composable () -> Unit = {
        NotekeaButton(
            text = stringResource(resource = Res.string.welcome_new_vault_cancel),
            onClick = onCancel,
            style = NotekeaButtonStyle.SECONDARY
        )
    }
    val create: @Composable () -> Unit = {
        NotekeaButton(
            text = stringResource(resource = Res.string.welcome_new_vault_confirm),
            onClick = onConfirm,
            enabled = !isBusy
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS, alignment = Alignment.End),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isBusy) NotekeaProgressIndicator(label = stringResource(resource = Res.string.welcome_busy))
        when (dialogButtonOrder) {
            DialogButtonOrder.CANCEL_THEN_PRIMARY -> {
                cancel()
                create()
            }

            DialogButtonOrder.PRIMARY_THEN_CANCEL -> {
                create()
                cancel()
            }
        }
    }
}

private fun Modifier.cancelOnEscape(
    onCancel: () -> Unit
): Modifier = onPreviewKeyEvent { event ->
    val isEscape = event.type == KeyEventType.KeyDown && event.key == Key.Escape
    if (isEscape) onCancel()
    isEscape
}

private fun VaultError.isAboutName(): Boolean = this is VaultError.InvalidName || this is VaultError.FolderAlreadyExists

private fun newFolderPath(
    form: NewVaultForm,
    platformConventions: PlatformConventions
): String {
    val isWindows = platformConventions.operatingSystem == OperatingSystem.WINDOWS
    val separator = if (isWindows) WINDOWS_SEPARATOR else UNIX_SEPARATOR
    val folder = form.parentPath.removeSuffix(suffix = separator) + separator + form.name.trim()
    return displayPath(path = folder, platformConventions = platformConventions)
}

@Preview
@Composable
private fun NewVaultPanelPreview() {
    NotekeaPreviewThemes {
        Column(modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)) {
            NewVaultPanel(
                form = WelcomePreviewData.newVaultForm,
                platformConventions = WelcomePreviewData.platformConventions,
                onNameChange = {},
                onChangeLocation = {},
                onCancel = {},
                onConfirm = {},
                error = null
            )
        }
    }
}

@Preview
@Composable
private fun NewVaultPanelInvalidNamePreview() {
    NotekeaPreviewThemes {
        Column(modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)) {
            NewVaultPanel(
                form = WelcomePreviewData.newVaultForm.copy(name = PREVIEW_INVALID_NAME),
                platformConventions = WelcomePreviewData.platformConventions,
                onNameChange = {},
                onChangeLocation = {},
                onCancel = {},
                onConfirm = {},
                error = VaultError.InvalidName
            )
        }
    }
}

@Preview
@Composable
private fun NewVaultPanelBusyPreview() {
    NotekeaPreviewThemes {
        Column(modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)) {
            NewVaultPanel(
                form = WelcomePreviewData.newVaultForm,
                platformConventions = WelcomePreviewData.platformConventions,
                onNameChange = {},
                onChangeLocation = {},
                onCancel = {},
                onConfirm = {},
                error = null,
                isBusy = true
            )
        }
    }
}
