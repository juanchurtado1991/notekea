package app.notekea.notes.presentation.tree

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.components.NotekeaSplitPane
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.notes.domain.NoteEntry
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.TreePreferences
import kotlin.math.roundToInt

private const val PREVIEW_HEIGHT_DP = 480
private const val PREVIEW_WIDTH_DP = 900

/**
 * Stateless vault workspace: the resizable, hideable sidebar with the tree and, beside it, the selected note shown by
 * [noteContent] (the editor). The delete question is shown over both while it waits for an answer.
 */
@Composable
fun VaultTreeContent(
    state: VaultTreeState,
    vaultName: String,
    onIntent: (VaultTreeIntent) -> Unit,
    onSwitchVault: () -> Unit,
    onSearch: () -> Unit,
    noteContent: @Composable (NoteEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        NotekeaSplitPane(
            startWidth = state.sidebarWidthDp.dp,
            onStartWidthChange = { onIntent(VaultTreeIntent.OnSidebarResize(widthDp = it.value.roundToInt())) },
            isStartVisible = !state.isSidebarCollapsed,
            minStartWidth = TreePreferences.MIN_SIDEBAR_WIDTH_DP.dp,
            maxStartWidth = TreePreferences.MAX_SIDEBAR_WIDTH_DP.dp,
            start = {
                VaultSidebar(
                    state = state,
                    vaultName = vaultName,
                    onIntent = onIntent,
                    onSwitchVault = onSwitchVault,
                    onSearch = onSearch
                )
            },
            end = {
                NoteDetailsPanel(
                    state = state,
                    onShowSidebar = { onIntent(VaultTreeIntent.OnSidebarToggle) },
                    noteContent = noteContent,
                    onIntent = onIntent
                )
            }
        )
    }
    TreeQuestions(state = state, onIntent = onIntent)
}

@Composable
private fun TreeQuestions(
    state: VaultTreeState,
    onIntent: (VaultTreeIntent) -> Unit
) {
    state.pendingDelete?.let { path ->
        DeleteEntryDialog(
            path = path,
            tree = state.tree,
            buttonOrder = state.platformConventions.dialogButtonOrder,
            onConfirm = { onIntent(VaultTreeIntent.OnDeleteConfirm) },
            onDismiss = { onIntent(VaultTreeIntent.OnDeleteCancel) }
        )
    }
}

@Composable
private fun VaultTreeContentPreviewFrame(
    state: VaultTreeState
) {
    NotekeaPreviewThemes {
        VaultTreeContent(
            state = state,
            vaultName = TreePreviewData.VAULT_NAME,
            onIntent = {},
            onSwitchVault = {},
            onSearch = {},
            noteContent = { PreviewNoteContent(note = it) },
            modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp).height(height = PREVIEW_HEIGHT_DP.dp)
        )
    }
}

@Preview
@Composable
private fun VaultTreeContentPreview() {
    VaultTreeContentPreviewFrame(state = TreePreviewData.state())
}

@Preview
@Composable
private fun VaultTreeContentLoadingPreview() {
    VaultTreeContentPreviewFrame(state = TreePreviewData.state(selected = null).copy(isLoading = true))
}

@Preview
@Composable
private fun VaultTreeContentEmptyPreview() {
    VaultTreeContentPreviewFrame(state = TreePreviewData.state(selected = null).copy(rows = emptyList()))
}

@Preview
@Composable
private fun VaultTreeContentSidebarHiddenPreview() {
    VaultTreeContentPreviewFrame(state = TreePreviewData.state().copy(isSidebarCollapsed = true))
}

@Preview
@Composable
private fun VaultTreeContentLinksUpdatedPreview() {
    VaultTreeContentPreviewFrame(
        state = TreePreviewData.state().copy(
            notice = TreeNotice.LinksUpdated(notes = listOf("2026-10-20", "Welcome"), skippedCount = 0)
        )
    )
}

@Preview
@Composable
private fun VaultTreeContentDeletePreview() {
    VaultTreeContentPreviewFrame(state = TreePreviewData.state().copy(pendingDelete = RelativePath(value = "Projects")))
}
