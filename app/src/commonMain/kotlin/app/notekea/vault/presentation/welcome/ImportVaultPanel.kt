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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.platform.domain.DialogButtonOrder
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.designsystem.components.NotekeaButton
import app.notekea.core.presentation.designsystem.components.NotekeaButtonStyle
import app.notekea.core.presentation.designsystem.components.NotekeaCard
import app.notekea.core.presentation.designsystem.components.feedback.NotekeaProgressIndicator
import app.notekea.core.presentation.designsystem.components.fields.NotekeaTextField
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.platform.displayPath
import app.notekea.resources.Res
import app.notekea.resources.welcome_import_address_label
import app.notekea.resources.welcome_import_address_placeholder
import app.notekea.resources.welcome_import_busy
import app.notekea.resources.welcome_import_confirm
import app.notekea.resources.welcome_import_name_label
import app.notekea.resources.welcome_import_summary
import app.notekea.resources.welcome_import_title
import app.notekea.resources.welcome_import_token_hint
import app.notekea.resources.welcome_import_token_label
import app.notekea.resources.welcome_new_vault_cancel
import app.notekea.resources.welcome_new_vault_change_location
import app.notekea.resources.welcome_new_vault_location_label
import app.notekea.vault.domain.VaultError
import org.jetbrains.compose.resources.stringResource

private const val PREVIEW_WIDTH_DP = 520

/**
 * Inline "Import from a repository" panel (the developer's choice of 2026-11-14): the repository's address, the token
 * a private one needs, and the folder it is brought into, named after the repository. Each problem shows under the
 * field it is about; while it imports the fields wait and Cancel stops it. Enter imports and Esc cancels.
 */
@Composable
internal fun ImportVaultPanel(
    form: ImportVaultForm,
    platformConventions: PlatformConventions,
    onIntent: (WelcomeIntent) -> Unit,
    error: VaultError?,
    isBusy: Boolean = false
) {
    val addressFocus = remember { FocusRequester() }
    val onConfirm = { onIntent(WelcomeIntent.OnImportConfirm) }
    val onCancel = { onIntent(WelcomeIntent.OnImportCancel) }
    val place = ImportProblemPlace.of(error = error)

    val message = error?.let {
        vaultErrorMessage(
            error = it,
            platformConventions = platformConventions
        )
    }

    LaunchedEffect(key1 = Unit) { addressFocus.requestFocus() }

    NotekeaCard(modifier = Modifier.fillMaxWidth().cancelImportOnEscape(onCancel = onCancel)) {
        Column(
            modifier = Modifier.padding(all = NotekeaDimensions.SpacingL),
            verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingM)
        ) {
            Text(
                text = stringResource(resource = Res.string.welcome_import_title),
                style = MaterialTheme.typography.titleMedium
            )

            ImportField(
                label = stringResource(resource = Res.string.welcome_import_address_label),
                value = form.url,
                onValueChange = { onIntent(WelcomeIntent.OnImportUrlChange(url = it)) },
                modifier = Modifier.focusRequester(focusRequester = addressFocus),
                placeholder = stringResource(Res.string.welcome_import_address_placeholder),
                errorText = message.takeIf {
                    place == ImportProblemPlace.ADDRESS
                },
                onSubmit = onConfirm.takeIf {
                    !isBusy
                }
            )

            ImportField(
                label = stringResource(resource = Res.string.welcome_import_token_label),
                value = form.token,
                onValueChange = { onIntent(WelcomeIntent.OnImportTokenChange(token = it)) },
                placeholder = stringResource(resource = Res.string.welcome_import_token_hint),
                errorText = message.takeIf {
                    place == ImportProblemPlace.TOKEN
                },
                isSecret = true,
                onSubmit = onConfirm.takeIf {
                    !isBusy
                }
            )

            ImportField(
                label = stringResource(resource = Res.string.welcome_import_name_label),
                value = form.name,
                onValueChange = { onIntent(WelcomeIntent.OnImportNameChange(name = it)) },
                errorText = message.takeIf {
                    place == ImportProblemPlace.NAME
                },
                onSubmit = onConfirm.takeIf {
                    !isBusy
                }
            )

            ImportLocation(
                form = form,
                platformConventions = platformConventions,
                onChangeLocation = { onIntent(WelcomeIntent.OnImportLocationClick) },
                isBusy = isBusy,
                errorText = message.takeIf {
                    place == ImportProblemPlace.LOCATION
                }
            )

            ImportButtons(
                dialogButtonOrder = platformConventions.dialogButtonOrder,
                onCancel = onCancel,
                onConfirm = onConfirm,
                isBusy = isBusy
            )
        }
    }
}

@Composable
private fun ImportField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    errorText: String? = null,
    isSecret: Boolean = false,
    onSubmit: (() -> Unit)? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        NotekeaTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier.fillMaxWidth(),
            placeholder = placeholder,
            errorText = errorText,
            isSecret = isSecret,
            onSubmit = onSubmit
        )
    }
}

@Composable
private fun ImportLocation(
    form: ImportVaultForm,
    platformConventions: PlatformConventions,
    onChangeLocation: () -> Unit,
    isBusy: Boolean,
    errorText: String?
) {
    val colors = MaterialTheme.colorScheme

    Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)) {
        Text(
            text = stringResource(resource = Res.string.welcome_new_vault_location_label),
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = displayPath(
                    path = form.parentPath,
                    platformConventions = platformConventions
                ),
                modifier = Modifier.weight(weight = 1f),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            NotekeaButton(
                text = stringResource(resource = Res.string.welcome_new_vault_change_location),
                onClick = onChangeLocation,
                style = NotekeaButtonStyle.SECONDARY,
                enabled = !isBusy
            )
        }

        Text(
            text = errorText ?: stringResource(resource = Res.string.welcome_import_summary),
            style = MaterialTheme.typography.bodySmall,
            color = if (errorText != null) colors.error else colors.onSurfaceVariant
        )
    }
}

@Composable
private fun ImportButtons(
    dialogButtonOrder: DialogButtonOrder,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    isBusy: Boolean
) {
    // Cancel stays live while it imports: it is how a slow or wrong repository is stopped.
    val cancel: @Composable () -> Unit = {
        NotekeaButton(
            text = stringResource(resource = Res.string.welcome_new_vault_cancel),
            onClick = onCancel,
            style = NotekeaButtonStyle.SECONDARY
        )
    }

    val import: @Composable () -> Unit = {
        NotekeaButton(
            text = stringResource(resource = Res.string.welcome_import_confirm),
            onClick = onConfirm,
            enabled = !isBusy
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            space = NotekeaDimensions.SpacingS,
            alignment = Alignment.End
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isBusy) {
            val busy = stringResource(resource = Res.string.welcome_import_busy)
            NotekeaProgressIndicator(label = busy)

            Text(
                text = stringResource(resource = Res.string.welcome_import_busy),
                modifier = Modifier.weight(weight = 1f),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        when (dialogButtonOrder) {
            DialogButtonOrder.CANCEL_THEN_PRIMARY -> {
                cancel()
                import()
            }

            DialogButtonOrder.PRIMARY_THEN_CANCEL -> {
                import()
                cancel()
            }
        }
    }
}

private fun Modifier.cancelImportOnEscape(onCancel: () -> Unit): Modifier =
    onPreviewKeyEvent { event ->
        val isEscape = event.type == KeyEventType.KeyDown && event.key == Key.Escape
        if (isEscape) onCancel()
        isEscape
    }

@Preview
@Composable
private fun ImportVaultPanelPreview() {
    NotekeaPreviewThemes {
        Column(modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)) {
            ImportVaultPanel(
                form = WelcomePreviewData.importForm,
                platformConventions = WelcomePreviewData.platformConventions,
                onIntent = {},
                error = VaultError.RepositoryRefused
            )
        }
    }
}
