package app.notekea.sync.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.settings.SettingsSpotlight
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/** Settings → Sync for the open vault ([vaultPath], null with none), showing the help's [spotlight]; for Settings. */
@Composable
fun SyncSettingsScreen(
    vaultPath: String?,
    modifier: Modifier = Modifier,
    spotlight: SettingsSpotlight? = null,
    viewModel: SyncSettingsViewModel = koinViewModel(key = "SyncSettings:$vaultPath") {
        parametersOf(vaultPath)
    }
) {
    val state by viewModel.state.collectAsState()

    SyncSettingsContent(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
        spotlight = spotlight
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
