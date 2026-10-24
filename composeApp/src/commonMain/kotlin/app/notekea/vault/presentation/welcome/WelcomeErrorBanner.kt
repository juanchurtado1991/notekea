package app.notekea.vault.presentation.welcome

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.components.ErrorBanner
import app.notekea.core.presentation.components.NotekeaButton
import app.notekea.core.presentation.components.NotekeaButtonStyle
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.resources.Res
import app.notekea.resources.error_unexpected
import app.notekea.resources.welcome_recent_locate
import app.notekea.vault.domain.VaultError
import org.jetbrains.compose.resources.stringResource

private const val PREVIEW_MISSING_PATH = "/home/ana/Old/Archive"

/**
 * The welcome screen's error in plain language; null means an unexpected failure. A missing folder offers
 * "Locate…" so the error is never a dead end.
 */
@Composable
internal fun WelcomeErrorBanner(
    platformConventions: PlatformConventions,
    onDismiss: () -> Unit,
    onLocate: (missingPath: String) -> Unit,
    error: VaultError?,
    modifier: Modifier = Modifier
) {
    ErrorBanner(
        message = error?.let { vaultErrorMessage(error = it, platformConventions = platformConventions) }
            ?: stringResource(resource = Res.string.error_unexpected),
        modifier = modifier,
        onDismiss = onDismiss,
        action = (error as? VaultError.FolderMissing)?.let { missing ->
            {
                NotekeaButton(
                    text = stringResource(resource = Res.string.welcome_recent_locate),
                    onClick = { onLocate(missing.path) },
                    style = NotekeaButtonStyle.SECONDARY
                )
            }
        }
    )
}

@Preview
@Composable
private fun WelcomeErrorBannerPreview() {
    NotekeaPreviewThemes {
        WelcomeErrorBanner(
            platformConventions = WelcomePreviewData.platformConventions,
            onDismiss = {},
            onLocate = {},
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
            onDismiss = {},
            onLocate = {},
            error = null
        )
    }
}
