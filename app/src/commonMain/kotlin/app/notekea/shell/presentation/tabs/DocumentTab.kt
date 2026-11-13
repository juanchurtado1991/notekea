package app.notekea.shell.presentation.tabs

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.documents.domain.DocumentFormat
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.documents.presentation.DocumentViewerContent
import app.notekea.documents.presentation.DocumentViewerScreen
import app.notekea.documents.presentation.DocumentViewerState
import app.notekea.shell.presentation.workspace.PaneContext

/** A document's tab (4.5): its read-only viewer, at the passage a citation or a search result asked for. */
@Composable
internal fun DocumentTab(
    path: RelativePath,
    context: PaneContext
) {
    val reveal = context
        .viewModels
        .documentFocus
        .request
        ?.takeIf {
            it.path == path
        }

    DocumentViewerScreen(
        vaultPath = context.state.vaultPath,
        path = path,
        passage = reveal?.range,
        count = reveal?.count ?: 0
    )
}

@Preview
@Composable
private fun DocumentTabPreview() {
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
