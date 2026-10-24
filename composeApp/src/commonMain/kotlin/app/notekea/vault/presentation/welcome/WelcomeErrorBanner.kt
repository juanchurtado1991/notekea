package app.notekea.vault.presentation.welcome

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.components.ErrorBanner
import app.notekea.core.presentation.components.NotekeaButton
import app.notekea.core.presentation.components.NotekeaButtonStyle
import app.notekea.core.presentation.platform.displayPath
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.resources.Res
import app.notekea.resources.error_unexpected
import app.notekea.resources.error_vault_creation_failed
import app.notekea.resources.error_vault_folder_exists
import app.notekea.resources.error_vault_folder_missing
import app.notekea.resources.error_vault_invalid_name
import app.notekea.resources.error_vault_permission_denied
import app.notekea.resources.welcome_recent_locate
import app.notekea.vault.domain.VaultError
import org.jetbrains.compose.resources.stringResource

private const val PREVIEW_MISSING_PATH = "/home/ana/Old/Archive"

/** The welcome flow's error in plain language; a missing folder offers "Locate…" so the error is never a dead end. */
@Composable
internal fun WelcomeErrorBanner(
    platformConventions: PlatformConventions,
    onIntent: (WelcomeIntent) -> Unit,
    error: VaultError?,
    modifier: Modifier = Modifier
) {
    val message = error?.let { vaultErrorMessage(error = it, platformConventions = platformConventions) }
    ErrorBanner(
        message = message ?: stringResource(resource = Res.string.error_unexpected),
        modifier = modifier,
        onDismiss = { onIntent(WelcomeIntent.OnErrorDismiss) },
        action = (error as? VaultError.FolderMissing)?.let { missing ->
            {
                NotekeaButton(
                    text = stringResource(resource = Res.string.welcome_recent_locate),
                    onClick = { onIntent(WelcomeIntent.OnLocateVaultClick(missingPath = missing.path)) },
                    style = NotekeaButtonStyle.SECONDARY
                )
            }
        }
    )
}

@Composable
private fun vaultErrorMessage(
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
private fun WelcomeErrorBannerPreview() {
    NotekeaPreviewThemes {
        WelcomeErrorBanner(
            platformConventions = WelcomePreviewData.platformConventions,
            onIntent = {},
            error = VaultError.FolderMissing(path = PREVIEW_MISSING_PATH)
        )
    }
}

@Preview
@Composable
private fun WelcomeErrorBannerUnexpectedPreview() {
    NotekeaPreviewThemes {
        WelcomeErrorBanner(
            platformConventions = WelcomePreviewData.platformConventions,
            onIntent = {},
            error = null
        )
    }
}
