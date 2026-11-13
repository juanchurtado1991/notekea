package app.notekea.tree.presentation.view

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.platform.domain.stacksAlertButtons
import app.notekea.core.presentation.designsystem.components.layout.NotekeaSplitPane
import app.notekea.core.presentation.designsystem.components.layout.NotekeaTitleBarBand
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.sidebarSurface
import app.notekea.core.presentation.platform.LocalTitleBarSpace
import app.notekea.tree.domain.TreePreferences
import app.notekea.tree.presentation.VaultSidebar
import app.notekea.tree.presentation.filepreview.NoteDetailsPanel
import app.notekea.tree.presentation.filepreview.PreviewNoteContent
import app.notekea.tree.presentation.model.TreeNotice
import app.notekea.tree.presentation.model.TreePreviewData
import app.notekea.tree.presentation.model.VaultTreeIntent
import app.notekea.tree.presentation.model.VaultTreeState
import kotlin.math.roundToInt

private const val PREVIEW_HEIGHT_DP = 480
private const val PREVIEW_NARROW_WIDTH_DP = 640
private const val PREVIEW_WIDTH_DP = 900

/**
 * Stateless vault workspace: the tool rail on the left edge (its buttons beyond the tree doing [rail]'s actions, the
 * side panel's selected while [isSidePanelVisible]), the resizable, hideable sidebar with the tree and, beside it,
 * [content]: the editor's tabs and panes. In a narrow window the sidebar floats over them.
 */
@Composable
fun VaultTreeContent(
    state: VaultTreeState,
    vaultName: String,
    onIntent: (VaultTreeIntent) -> Unit,
    rail: SidebarRailActions,
    modifier: Modifier = Modifier,
    isSidePanelVisible: Boolean = false,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.sidebarSurface,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        BoxWithConstraints {
            val isOverlay = maxWidth < TreePreferences.SIDEBAR_OVERLAY_BELOW_DP.dp

            val titleBar = LocalTitleBarSpace.current
            val isTreeShown = !state.isSidebarCollapsed && !isOverlay
            val railWidth = NotekeaDimensions.ToolRailWidth

            Row {
                // On macOS the window's buttons sit in the sidebar's color, over the rail and the tree (1A).
                Column {
                    NotekeaTitleBarBand(
                        modifier = Modifier.width(width = NotekeaDimensions.ToolRailWidth)
                    )

                    SidebarRail(
                        state = state,
                        onIntent = onIntent,
                        actions = rail,
                        isSidePanelVisible = isSidePanelVisible
                    )
                }

                NotekeaSplitPane(
                    panelWidth = state.sidebarWidthDp.dp,
                    onPanelWidthChange = {
                        onIntent(VaultTreeIntent.OnSidebarResize(widthDp = it.value.roundToInt()))
                    },
                    isPanelVisible = !state.isSidebarCollapsed,
                    minPanelWidth = TreePreferences.MIN_SIDEBAR_WIDTH_DP.dp,
                    maxPanelWidth = TreePreferences.MAX_SIDEBAR_WIDTH_DP.dp,
                    panel = {
                        Column {
                            NotekeaTitleBarBand(modifier = Modifier.fillMaxWidth())

                            VaultSidebar(
                                state = state,
                                vaultName = vaultName,
                                onIntent = onIntent
                            )
                        }
                    },
                    content = {
                        // Without the tree beside them, the tabs leave the window buttons their room.
                        CompositionLocalProvider(
                            LocalTitleBarSpace provides titleBar?.withLeading(
                                leading = if (isTreeShown) 0.dp else titleBar.leading - railWidth
                            )
                        ) {
                            NoteDetailsPanel(
                                state = state,
                                content = content
                            )
                        }
                    },
                    isOverlay = isOverlay
                )
            }
        }
    }

    TreeQuestions(
        state = state,
        onIntent = onIntent
    )
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
            onDismiss = { onIntent(VaultTreeIntent.OnDeleteCancel) },
            isStacked = state.platformConventions.stacksAlertButtons
        )
    }
}

@Composable
private fun VaultTreeContentPreviewFrame(
    state: VaultTreeState,
    widthDp: Int = PREVIEW_WIDTH_DP
) {
    NotekeaPreviewThemes {
        VaultTreeContent(
            state = state,
            vaultName = TreePreviewData.VAULT_NAME,
            onIntent = {},
            rail = SidebarRailActions(),
            modifier = Modifier.width(width = widthDp.dp).height(height = PREVIEW_HEIGHT_DP.dp)
        ) { PreviewNoteContent(path = state.selectedPath) }
    }
}

@Preview
@Composable
private fun VaultTreeContentPreview() {
    VaultTreeContentPreviewFrame(state = TreePreviewData.state())
}

@Preview
@Composable
private fun VaultTreeContentNarrowWindowPreview() {
    VaultTreeContentPreviewFrame(
        state = TreePreviewData.state(),
        widthDp = PREVIEW_NARROW_WIDTH_DP
    )
}

@Preview
@Composable
private fun VaultTreeContentLoadingPreview() {
    VaultTreeContentPreviewFrame(
        state = TreePreviewData.state(selected = null).copy(isLoading = true)
    )
}

@Preview
@Composable
private fun VaultTreeContentEmptyPreview() {
    VaultTreeContentPreviewFrame(
        state = TreePreviewData.state(selected = null).copy(rows = emptyList())
    )
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
            notice = TreeNotice.LinksUpdated(
                notes = listOf("2026-10-20", "Welcome"),
                skippedCount = 0
            )
        )
    )
}

@Preview
@Composable
private fun VaultTreeContentDeletePreview() {
    VaultTreeContentPreviewFrame(
        state = TreePreviewData.state().copy(pendingDelete = RelativePath(value = "Projects"))
    )
}
