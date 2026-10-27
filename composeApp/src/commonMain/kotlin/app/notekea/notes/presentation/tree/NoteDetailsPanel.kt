package app.notekea.notes.presentation.tree

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.components.NotekeaIconButton
import app.notekea.core.presentation.platform.label
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.resources.Res
import app.notekea.resources.tree_show_sidebar
import org.jetbrains.compose.resources.stringResource

private const val PREVIEW_HEIGHT_DP = 320
private const val PREVIEW_WIDTH_DP = 520

/**
 * The main area beside the sidebar: the editor's tabs and panes ([content], `ROADMAP.md` 1.11), blank while the vault
 * loads. When the sidebar is hidden, a button brings it back.
 */
@Composable
internal fun NoteDetailsPanel(
    state: VaultTreeState,
    onShowSidebar: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (!state.isLoading) content()
        if (state.isSidebarCollapsed) {
            NotekeaIconButton(
                icon = NotekeaIcons.ShowSidebar,
                label = stringResource(resource = Res.string.tree_show_sidebar),
                onClick = onShowSidebar,
                modifier = Modifier.padding(all = NotekeaDimensions.SpacingS),
                shortcutHint = state.platformConventions.toggleSidebarShortcut.label(
                    operatingSystem = state.platformConventions.operatingSystem
                )
            )
        }
    }
}

@Composable
private fun NoteDetailsPanelPreviewFrame(
    state: VaultTreeState
) {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp).height(height = PREVIEW_HEIGHT_DP.dp)) {
            NoteDetailsPanel(state = state, onShowSidebar = {}) { PreviewNoteContent(path = state.selectedPath) }
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
