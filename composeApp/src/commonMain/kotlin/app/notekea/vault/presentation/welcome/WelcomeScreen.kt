package app.notekea.vault.presentation.welcome

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.platform.WindowShortcut
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.vault.domain.Vault
import org.koin.compose.viewmodel.koinViewModel

/**
 * Welcome flow entry point: collects [WelcomeViewModel] and renders [WelcomeContent]. The open-folder shortcut works
 * whatever has focus while the screen is shown. [onVaultNameChange] reports the open vault's name (or null) so the
 * window title can show it; [openedVaultContent] draws the open vault and gets the action that returns here.
 */
@Composable
fun WelcomeScreen(
    onVaultNameChange: (String?) -> Unit,
    openedVaultContent: @Composable (Vault, onSwitchVault: () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WelcomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val vaultName = state.openedVault?.name
    LaunchedEffect(key1 = vaultName) { onVaultNameChange(vaultName) }
    WindowShortcut(
        shortcut = state.platformConventions.openFolderShortcut,
        onTrigger = { viewModel.onIntent(intent = WelcomeIntent.OnOpenFolderClick) }
    )
    WelcomeContent(
        state = state,
        onIntent = viewModel::onIntent,
        openedVaultContent = openedVaultContent,
        modifier = modifier
    )
}

@Preview
@Composable
private fun WelcomeScreenPreview() {
    NotekeaPreviewThemes {
        WelcomeContent(
            state = WelcomePreviewData.state().copy(recentVaults = WelcomePreviewData.recentVaults),
            onIntent = {},
            openedVaultContent = { _, _ -> }
        )
    }
}
