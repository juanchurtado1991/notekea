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

private const val PREVIEW_WIDTH_DP = 520
private const val UNIX_SEPARATOR = "/"
private const val WINDOWS_SEPARATOR = "\\"

/**
 * Inline "Create a new vault" panel: a suggested name and location, so a first-time user only has to press
 * "Create vault", and a sentence saying exactly which folder will be created. Buttons follow the platform order.
 */
@Composable
internal fun NewVaultPanel(
    form: NewVaultForm,
    platformConventions: PlatformConventions,
    onIntent: (WelcomeIntent) -> Unit,
    enabled: Boolean = true
) {
    NotekeaCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(all = NotekeaDimensions.SpacingL),
            verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingM)
        ) {
            Text(
                text = stringResource(resource = Res.string.welcome_new_vault_title),
                style = MaterialTheme.typography.titleMedium
            )
            FieldLabel(text = stringResource(resource = Res.string.welcome_new_vault_name_label))
            NotekeaTextField(
                value = form.name,
                onValueChange = { onIntent(WelcomeIntent.OnNewVaultNameChange(name = it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = stringResource(resource = Res.string.welcome_new_vault_default_name)
            )
            FieldLabel(text = stringResource(resource = Res.string.welcome_new_vault_location_label))
            LocationRow(form = form, platformConventions = platformConventions, enabled = enabled, onIntent = onIntent)
            Text(
                text = stringResource(Res.string.welcome_new_vault_summary, newFolderPath(form, platformConventions)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            PanelButtons(
                dialogButtonOrder = platformConventions.dialogButtonOrder,
                canCreate = enabled && form.name.isNotBlank(),
                onIntent = onIntent
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
    enabled: Boolean,
    onIntent: (WelcomeIntent) -> Unit
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
            onClick = { onIntent(WelcomeIntent.OnNewVaultLocationClick) },
            style = NotekeaButtonStyle.SECONDARY,
            enabled = enabled
        )
    }
}

@Composable
private fun PanelButtons(
    dialogButtonOrder: DialogButtonOrder,
    canCreate: Boolean,
    onIntent: (WelcomeIntent) -> Unit
) {
    val cancel: @Composable () -> Unit = {
        NotekeaButton(
            text = stringResource(resource = Res.string.welcome_new_vault_cancel),
            onClick = { onIntent(WelcomeIntent.OnNewVaultCancel) },
            style = NotekeaButtonStyle.SECONDARY
        )
    }
    val create: @Composable () -> Unit = {
        NotekeaButton(
            text = stringResource(resource = Res.string.welcome_new_vault_confirm),
            onClick = { onIntent(WelcomeIntent.OnNewVaultConfirm) },
            enabled = canCreate
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS, alignment = Alignment.End)
    ) {
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
                onIntent = {}
            )
        }
    }
}
