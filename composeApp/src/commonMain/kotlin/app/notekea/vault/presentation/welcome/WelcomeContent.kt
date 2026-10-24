package app.notekea.vault.presentation.welcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.components.NotekeaButton
import app.notekea.core.presentation.components.NotekeaButtonStyle
import app.notekea.core.presentation.components.NotekeaCard
import app.notekea.core.presentation.components.NotekeaWordmark
import app.notekea.core.presentation.platform.label
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.resources.Res
import app.notekea.resources.welcome_create_vault
import app.notekea.resources.welcome_new_vault_default_name
import app.notekea.resources.welcome_open_folder
import app.notekea.resources.welcome_open_hint
import app.notekea.resources.welcome_recent_title
import app.notekea.resources.welcome_tagline
import app.notekea.vault.domain.VaultError
import app.notekea.vault.presentation.home.VaultHomeContent
import org.jetbrains.compose.resources.stringResource

private const val CONTENT_MAX_WIDTH_DP = 560
private const val PREVIEW_HEIGHT_DP = 640
private const val TOP_SPACE_DP = 72

/**
 * Stateless welcome flow: nothing but the wordmark while the last vault is restored, the open vault once there is
 * one, and otherwise the welcome screen (`ROADMAP.md` 1.3) centered in a column narrow enough to read at a glance.
 */
@Composable
fun WelcomeContent(
    state: WelcomeState,
    onIntent: (WelcomeIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Box {
            val openedVault = state.openedVault
            when {
                state.isLoading -> NotekeaWordmark(modifier = Modifier.align(alignment = Alignment.Center))

                openedVault != null -> VaultHomeContent(
                    openedVault = openedVault,
                    platformConventions = state.platformConventions,
                    onSwitchVault = { onIntent(WelcomeIntent.OnSwitchVaultClick) }
                )

                else -> WelcomeHome(state = state, onIntent = onIntent)
            }
        }
    }
}

@Composable
private fun WelcomeHome(
    state: WelcomeState,
    onIntent: (WelcomeIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState())
            .padding(horizontal = NotekeaDimensions.SpacingXl, vertical = NotekeaDimensions.SpacingXxl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.widthIn(max = CONTENT_MAX_WIDTH_DP.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingL)
        ) {
            Spacer(modifier = Modifier.height(height = TOP_SPACE_DP.dp))
            WelcomeHeader()
            Spacer(modifier = Modifier.height(height = NotekeaDimensions.SpacingS))
            val showError = state.error != null || state.hasUnexpectedError
            if (showError) {
                WelcomeErrorBanner(
                    platformConventions = state.platformConventions,
                    onIntent = onIntent,
                    error = state.error,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            WelcomeStart(state = state, onIntent = onIntent)
            if (state.recentVaults.isNotEmpty()) RecentVaultsSection(state = state, onIntent = onIntent)
        }
    }
}

@Composable
private fun WelcomeHeader() {
    NotekeaWordmark()
    Text(
        text = stringResource(resource = Res.string.welcome_tagline),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun WelcomeStart(
    state: WelcomeState,
    onIntent: (WelcomeIntent) -> Unit
) {
    val form = state.newVaultForm
    if (form != null) {
        NewVaultPanel(
            form = form,
            platformConventions = state.platformConventions,
            onIntent = onIntent,
            enabled = !state.isBusy
        )
        return
    }
    val conventions = state.platformConventions
    val suggestedName = stringResource(resource = Res.string.welcome_new_vault_default_name)
    Row(horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingM)) {
        NotekeaButton(
            text = stringResource(resource = Res.string.welcome_open_folder),
            onClick = { onIntent(WelcomeIntent.OnOpenFolderClick) },
            enabled = !state.isBusy,
            shortcutHint = conventions.openFolderShortcut.label(operatingSystem = conventions.operatingSystem)
        )
        NotekeaButton(
            text = stringResource(resource = Res.string.welcome_create_vault),
            onClick = { onIntent(WelcomeIntent.OnCreateVaultClick(suggestedName = suggestedName)) },
            style = NotekeaButtonStyle.SECONDARY,
            enabled = !state.isBusy
        )
    }
    Text(
        text = stringResource(resource = Res.string.welcome_open_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun RecentVaultsSection(
    state: WelcomeState,
    onIntent: (WelcomeIntent) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = NotekeaDimensions.SpacingXl),
        verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)
    ) {
        Text(
            text = stringResource(resource = Res.string.welcome_recent_title),
            modifier = Modifier.padding(horizontal = NotekeaDimensions.SpacingXs),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        NotekeaCard(modifier = Modifier.fillMaxWidth()) {
            state.recentVaults.forEach {
                RecentVaultRow(
                    recentVault = it,
                    platformConventions = state.platformConventions,
                    onIntent = onIntent,
                    enabled = !state.isBusy
                )
            }
        }
    }
}

@Composable
private fun WelcomeContentPreviewFrame(
    state: WelcomeState
) {
    NotekeaPreviewThemes {
        WelcomeContent(state = state, onIntent = {}, modifier = Modifier.height(height = PREVIEW_HEIGHT_DP.dp))
    }
}

@Preview
@Composable
private fun WelcomeContentLoadingPreview() {
    WelcomeContentPreviewFrame(state = WelcomePreviewData.state().copy(isLoading = true))
}

@Preview
@Composable
private fun WelcomeContentNoRecentsPreview() {
    WelcomeContentPreviewFrame(state = WelcomePreviewData.state())
}

@Preview
@Composable
private fun WelcomeContentRecentsPreview() {
    WelcomeContentPreviewFrame(state = WelcomePreviewData.state().copy(recentVaults = WelcomePreviewData.recentVaults))
}

@Preview
@Composable
private fun WelcomeContentNewVaultPreview() {
    WelcomeContentPreviewFrame(
        state = WelcomePreviewData.state().copy(
            recentVaults = WelcomePreviewData.recentVaults,
            newVaultForm = WelcomePreviewData.newVaultForm
        )
    )
}

@Preview
@Composable
private fun WelcomeContentPermissionErrorPreview() {
    WelcomeContentPreviewFrame(
        state = WelcomePreviewData.state().copy(
            recentVaults = WelcomePreviewData.recentVaults,
            error = VaultError.PermissionDenied(path = WelcomePreviewData.openedVault.vault.path)
        )
    )
}

@Preview
@Composable
private fun WelcomeContentVaultOpenedPreview() {
    WelcomeContentPreviewFrame(state = WelcomePreviewData.state().copy(openedVault = WelcomePreviewData.openedVault))
}
