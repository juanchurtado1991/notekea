package app.notekea.vault.presentation.welcome

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.platform.domain.Shortcut
import app.notekea.core.presentation.platform.LocalShortcutDispatcher
import app.notekea.core.presentation.platform.ShortcutDispatcher
import app.notekea.core.presentation.platform.matches
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import org.koin.compose.viewmodel.koinViewModel

/**
 * Welcome flow entry point: collects [WelcomeViewModel] and renders [WelcomeContent]. The open-folder shortcut is
 * registered with the window's [ShortcutDispatcher] while the screen is shown, so it works whatever has focus.
 * [onVaultNameChange] reports the open vault's name (or null) so the window title can show it.
 */
@Composable
fun WelcomeScreen(
    onVaultNameChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WelcomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val vaultName = state.openedVault?.vault?.name
    LaunchedEffect(key1 = vaultName) { onVaultNameChange(vaultName) }
    OpenFolderShortcut(
        shortcut = state.platformConventions.openFolderShortcut,
        onTrigger = { viewModel.onIntent(intent = WelcomeIntent.OnOpenFolderClick) }
    )
    WelcomeContent(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier
    )
}

@Composable
private fun OpenFolderShortcut(
    shortcut: Shortcut,
    onTrigger: () -> Unit
) {
    val dispatcher = LocalShortcutDispatcher.current
    val currentOnTrigger by rememberUpdatedState(newValue = onTrigger)
    DisposableEffect(dispatcher, shortcut) {
        val unregister = dispatcher.register { event ->
            val isOpenFolder = shortcut.matches(event = event)
            if (isOpenFolder) currentOnTrigger()
            isOpenFolder
        }
        onDispose { unregister() }
    }
}

@Preview
@Composable
private fun WelcomeScreenPreview() {
    NotekeaPreviewThemes {
        WelcomeContent(
            state = WelcomePreviewData.state().copy(recentVaults = WelcomePreviewData.recentVaults),
            onIntent = {}
        )
    }
}
