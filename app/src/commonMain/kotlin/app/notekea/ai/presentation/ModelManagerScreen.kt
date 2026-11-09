package app.notekea.ai.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import org.koin.compose.viewmodel.koinViewModel

/** Settings → AI model on its [ModelManagerViewModel]; the app gives it to Settings as its AI model section. */
@Composable
fun ModelManagerScreen(
    modifier: Modifier = Modifier,
    viewModel: ModelManagerViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    ModelManagerContent(state = state, onIntent = viewModel::onIntent, modifier = modifier)
}

@Preview
@Composable
private fun ModelManagerScreenPreview() {
    NotekeaPreviewThemes {
        ModelManagerContent(state = ModelManagerSamples.notInstalled, onIntent = {})
    }
}
