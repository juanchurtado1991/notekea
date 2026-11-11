package app.notekea.sync.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** Settings → Sync for the open vault ([vaultPath], null with none); the app gives it to Settings. */
@Composable
fun SyncSettingsScreen(
    vaultPath: String?,
    modifier: Modifier = Modifier,
    viewModel: SyncSettingsViewModel = koinViewModel(key = "SyncSettings:$vaultPath") {
        parametersOf(vaultPath)
    }
) {
    val state by viewModel.state.collectAsState()

    SyncSettingsContent(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier
    )
}

@Preview
@Composable
private fun SyncSettingsScreenPreview() {
    NotekeaPreviewThemes {
        SyncSettingsContent(
            state = SyncSettingsState(),
            onIntent = {}
        )
    }
}
