package app.notekea.shell.presentation.tabs

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.documents.domain.DocumentFormat
import app.notekea.core.notes.domain.AttachmentKind
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.documents.presentation.DocumentViewerContent
import app.notekea.documents.presentation.DocumentViewerState
import app.notekea.shell.presentation.workspace.PaneContext
import app.notekea.tree.presentation.filepreview.FileDetails
import app.notekea.tree.presentation.filepreview.FilePreview

/** A file's tab: a document read in its viewer (4.5), any other file as its preview with its app and its folder. */
@Composable
internal fun FileTab(
    path: RelativePath,
    context: PaneContext
) {
    if (DocumentFormat.of(path = path) != null) {
        return DocumentTab(
            path = path,
            context = context
        )
    }

    val state = context.state
    val file = state.tree.attachments[path] ?: return

    FileDetails(
        preview = context
            .workspaceState
            .previews[path]
            ?.takeIf {
                it.file == file
            }
            ?: FilePreview(
                file = file,
                kind = AttachmentKind.of(name = path.name)
            ),
        platformConventions = state.platformConventions,
        onIntent = context.viewModels.tree::onIntent
    )
}

@Preview
@Composable
private fun FileTabPreview() {
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
