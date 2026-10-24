package app.notekea.vault.presentation.welcome

import androidx.compose.foundation.focusable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.platform.matches
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import org.koin.compose.viewmodel.koinViewModel

/**
 * Welcome flow entry point: collects [WelcomeViewModel] and renders [WelcomeContent]. The root takes focus at start
 * so the open-folder shortcut works before anything is clicked; key events from focused children pass through it.
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
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(key1 = vaultName) { onVaultNameChange(vaultName) }
    LaunchedEffect(key1 = Unit) { focusRequester.requestFocus() }
    WelcomeContent(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier
            .onPreviewKeyEvent { event ->
                val isOpenFolder = state.platformConventions.openFolderShortcut.matches(event = event)
                if (isOpenFolder) viewModel.onIntent(intent = WelcomeIntent.OnOpenFolderClick)
                isOpenFolder
            }.focusRequester(focusRequester = focusRequester)
            .focusable()
    )
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
