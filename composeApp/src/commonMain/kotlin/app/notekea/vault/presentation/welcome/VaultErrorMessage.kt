package app.notekea.vault.presentation.welcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.platform.displayPath
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.resources.Res
import app.notekea.resources.error_vault_creation_failed
import app.notekea.resources.error_vault_folder_exists
import app.notekea.resources.error_vault_folder_missing
import app.notekea.resources.error_vault_invalid_name
import app.notekea.resources.error_vault_permission_denied
import app.notekea.vault.domain.VaultError
import org.jetbrains.compose.resources.stringResource

private const val PREVIEW_PATH = "/home/ana/Notes"

/** A [VaultError] in plain language, with its folder written the way users read paths (`~/Notes`). */
@Composable
internal fun vaultErrorMessage(
    error: VaultError,
    platformConventions: PlatformConventions
): String {
    val shown: (String) -> String = { displayPath(path = it, platformConventions = platformConventions) }
    return when (error) {
        is VaultError.CreationFailed -> stringResource(Res.string.error_vault_creation_failed, shown(error.path))
        is VaultError.FolderAlreadyExists -> stringResource(Res.string.error_vault_folder_exists, shown(error.path))
        is VaultError.FolderMissing -> stringResource(Res.string.error_vault_folder_missing, shown(error.path))
        VaultError.InvalidName -> stringResource(resource = Res.string.error_vault_invalid_name)
        is VaultError.PermissionDenied -> stringResource(Res.string.error_vault_permission_denied, shown(error.path))
    }
}

@Preview
@Composable
private fun vaultErrorMessagePreview() {
    val errors = listOf(
        VaultError.CreationFailed(path = PREVIEW_PATH),
        VaultError.FolderAlreadyExists(path = PREVIEW_PATH),
        VaultError.FolderMissing(path = PREVIEW_PATH),
        VaultError.InvalidName,
        VaultError.PermissionDenied(path = PREVIEW_PATH)
    )
    NotekeaPreviewThemes {
        Column(verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)) {
            errors.forEach {
                Text(
                    text = vaultErrorMessage(error = it, platformConventions = WelcomePreviewData.platformConventions),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
