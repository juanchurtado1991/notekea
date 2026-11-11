package app.notekea.documents.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.documents.domain.DocumentFormat
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * The read-only tab of the document at [path] in the vault at [vaultPath]; [passage] is the passage to show, asked for
 * the [count]th time, so the same one asked again scrolls to it again.
 */
@Composable
fun DocumentViewerScreen(
    vaultPath: String,
    path: RelativePath,
    modifier: Modifier = Modifier,
    passage: IntRange? = null,
    count: Int = 0,
    viewModel: DocumentViewerViewModel = koinViewModel(key = "Document:$vaultPath:${path.value}") {
        parametersOf(vaultPath, path)
    }
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel, passage, count) {
        passage?.let {
            viewModel.onIntent(
                intent = DocumentViewerIntent.OnReveal(
                    range = it,
                    count = count
                )
            )
        }
    }

    DisposableEffect(viewModel) {
        onDispose { viewModel.onIntent(intent = DocumentViewerIntent.OnHidden) }
    }

    DocumentViewerContent(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier
    )
}

@Preview
@Composable
private fun DocumentViewerScreenPreview() {
    NotekeaPreviewThemes {
        DocumentViewerContent(
            state = DocumentViewerState(
                name = "Report.pdf",
                format = DocumentFormat.PDF
            ),
            onIntent = {}
        )
    }
}
