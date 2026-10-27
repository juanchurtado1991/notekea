package app.notekea.notes.presentation.tree

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.preview.NotekeaPreviewThemes

private const val PREVIEW_HEIGHT_DP = 320
private const val PREVIEW_WIDTH_DP = 520

/**
 * The main area beside the sidebar: the editor's tabs and panes ([content], `ROADMAP.md` 1.11), blank while the vault
 * loads. The tool rail brings a hidden sidebar back, so nothing here covers the tabs.
 */
@Composable
internal fun NoteDetailsPanel(
    state: VaultTreeState,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (!state.isLoading) content()
    }
}

@Composable
private fun NoteDetailsPanelPreviewFrame(
    state: VaultTreeState
) {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp).height(height = PREVIEW_HEIGHT_DP.dp)) {
            NoteDetailsPanel(state = state) { PreviewNoteContent(path = state.selectedPath) }
        }
    }
}

@Preview
@Composable
private fun NoteDetailsPanelPreview() {
    NoteDetailsPanelPreviewFrame(state = TreePreviewData.state())
}

@Preview
@Composable
private fun NoteDetailsPanelSidebarHiddenPreview() {
    NoteDetailsPanelPreviewFrame(state = TreePreviewData.state().copy(isSidebarCollapsed = true))
}
