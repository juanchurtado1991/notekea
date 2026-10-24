package app.notekea.notes.presentation.tree

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.platform.WindowShortcut
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.resources.Res
import app.notekea.resources.tree_untitled_note
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Vault workspace entry point for the vault at [vaultPath]: one [VaultTreeViewModel] per vault (opening another vault
 * starts fresh), rendered by [VaultTreeContent]. The new-note and toggle-sidebar shortcuts work whatever has focus.
 */
@Composable
fun VaultTreeScreen(
    vaultPath: String,
    vaultName: String,
    onSwitchVault: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VaultTreeViewModel = koinViewModel(key = vaultPath) { parametersOf(vaultPath) }
) {
    val state by viewModel.state.collectAsState()
    val untitledNote = stringResource(resource = Res.string.tree_untitled_note)
    WindowShortcut(shortcut = state.platformConventions.newNoteShortcut) {
        viewModel.onIntent(intent = VaultTreeIntent.OnNewNoteClick(baseName = untitledNote))
    }
    WindowShortcut(shortcut = state.platformConventions.toggleSidebarShortcut) {
        viewModel.onIntent(intent = VaultTreeIntent.OnSidebarToggle)
    }
    VaultTreeContent(
        state = state,
        vaultName = vaultName,
        onIntent = viewModel::onIntent,
        onSwitchVault = onSwitchVault,
        modifier = modifier
    )
}

@Preview
@Composable
private fun VaultTreeScreenPreview() {
    NotekeaPreviewThemes {
        VaultTreeContent(state = TreePreviewData.state(), vaultName = TreePreviewData.VAULT_NAME, onIntent = {
        }, onSwitchVault = {})
    }
}
