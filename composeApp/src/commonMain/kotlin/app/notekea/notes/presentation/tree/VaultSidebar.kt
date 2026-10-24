package app.notekea.notes.presentation.tree

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.components.EmptyState
import app.notekea.core.presentation.components.ErrorBanner
import app.notekea.core.presentation.components.NotekeaButton
import app.notekea.core.presentation.components.NotekeaButtonStyle
import app.notekea.core.presentation.components.NotekeaSkeletonRow
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.notes.domain.NotesError
import app.notekea.resources.Res
import app.notekea.resources.error_unexpected
import app.notekea.resources.tree_empty_message
import app.notekea.resources.tree_empty_title
import app.notekea.resources.tree_loading
import app.notekea.resources.tree_moved_to_vault_trash
import app.notekea.resources.tree_new_note
import app.notekea.resources.tree_notice_dismiss
import app.notekea.resources.tree_retry
import app.notekea.resources.tree_switch_vault
import app.notekea.resources.tree_untitled_folder
import app.notekea.resources.tree_untitled_note
import org.jetbrains.compose.resources.stringResource

private const val PREVIEW_HEIGHT_DP = 520
private val SKELETON_ROWS = listOf(0 to 0.55f, 0 to 0.4f, 1 to 0.6f, 1 to 0.5f, 0 to 0.45f, 0 to 0.65f, 1 to 0.5f)

/**
 * The vault sidebar: header with the tree actions, then the tree in one of its states (skeleton while the first scan
 * runs, the error with Retry, the empty vault with a New note action, or the notes), and "Open another vault" at the
 * bottom so the sidebar is never a dead end. Action errors and notices sit above the tree until dismissed.
 */
@Composable
internal fun VaultSidebar(
    state: VaultTreeState,
    vaultName: String,
    onIntent: (VaultTreeIntent) -> Unit,
    onSwitchVault: () -> Unit
) {
    val untitledNote = stringResource(resource = Res.string.tree_untitled_note)
    val untitledFolder = stringResource(resource = Res.string.tree_untitled_folder)
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column {
            SidebarHeader(
                vaultName = vaultName,
                vaultPath = state.vaultPath,
                sort = state.sort,
                platformConventions = state.platformConventions,
                onNewNote = { onIntent(VaultTreeIntent.OnNewNoteClick(baseName = untitledNote)) },
                onNewFolder = { onIntent(VaultTreeIntent.OnNewFolderClick(baseName = untitledFolder)) },
                onSortToggle = { onIntent(VaultTreeIntent.OnSortToggle) },
                onHideSidebar = { onIntent(VaultTreeIntent.OnSidebarToggle) }
            )
            SidebarMessages(state = state, onIntent = onIntent)
            Box(modifier = Modifier.weight(weight = 1f).fillMaxWidth().padding(top = NotekeaDimensions.SpacingS)) {
                SidebarBody(state = state, onIntent = onIntent, untitledNote = untitledNote)
            }
            Divider()
            NotekeaButton(
                text = stringResource(resource = Res.string.tree_switch_vault),
                onClick = onSwitchVault,
                modifier = Modifier.padding(all = NotekeaDimensions.SpacingXs),
                style = NotekeaButtonStyle.TEXT
            )
        }
    }
}

@Composable
private fun SidebarBody(
    state: VaultTreeState,
    onIntent: (VaultTreeIntent) -> Unit,
    untitledNote: String
) {
    val loadError = state.loadError
    when {
        state.isLoading -> SkeletonTree()

        loadError != null -> ErrorBanner(
            message = notesErrorMessage(error = loadError, platformConventions = state.platformConventions),
            modifier = Modifier.padding(all = NotekeaDimensions.SpacingS),
            action = {
                NotekeaButton(
                    text = stringResource(resource = Res.string.tree_retry),
                    onClick = { onIntent(VaultTreeIntent.OnRetryClick) },
                    style = NotekeaButtonStyle.SECONDARY
                )
            }
        )

        state.rows.isEmpty() -> EmptyState(
            title = stringResource(resource = Res.string.tree_empty_title),
            message = stringResource(resource = Res.string.tree_empty_message),
            action = {
                NotekeaButton(
                    text = stringResource(resource = Res.string.tree_new_note),
                    onClick = { onIntent(VaultTreeIntent.OnNewNoteClick(baseName = untitledNote)) }
                )
            }
        )

        else -> TreeList(state = state, onIntent = onIntent)
    }
}

@Composable
private fun SidebarMessages(
    state: VaultTreeState,
    onIntent: (VaultTreeIntent) -> Unit
) {
    val actionError = state.actionError
    val messageModifier = Modifier.padding(
        start = NotekeaDimensions.SpacingS,
        end = NotekeaDimensions.SpacingS,
        top = NotekeaDimensions.SpacingS
    )
    if (actionError != null || state.hasUnexpectedError) {
        ErrorBanner(
            message =
                actionError?.let { notesErrorMessage(error = it, platformConventions = state.platformConventions) }
                    ?: stringResource(resource = Res.string.error_unexpected),
            modifier = messageModifier,
            onDismiss = { onIntent(VaultTreeIntent.OnErrorDismiss) }
        )
    }
    (state.notice as? TreeNotice.MovedToVaultTrash)?.let { notice ->
        Row(modifier = messageModifier, verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(Res.string.tree_moved_to_vault_trash, notice.name),
                modifier = Modifier.weight(weight = 1f),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            NotekeaButton(
                text = stringResource(resource = Res.string.tree_notice_dismiss),
                onClick = { onIntent(VaultTreeIntent.OnNoticeDismiss) },
                style = NotekeaButtonStyle.TEXT
            )
        }
    }
}

@Composable
private fun SkeletonTree() {
    val loading = stringResource(resource = Res.string.tree_loading)
    Column(modifier = Modifier.semantics { contentDescription = loading }) {
        SKELETON_ROWS.forEach { (depth, fraction) -> NotekeaSkeletonRow(widthFraction = fraction, depth = depth) }
    }
}

@Composable
private fun Divider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height = NotekeaDimensions.BorderWidth)
            .background(color = MaterialTheme.colorScheme.outline)
    )
}

@Composable
private fun SidebarPreviewFrame(
    state: VaultTreeState
) {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.width(width = state.sidebarWidthDp.dp).height(height = PREVIEW_HEIGHT_DP.dp)) {
            VaultSidebar(state = state, vaultName = TreePreviewData.VAULT_NAME, onIntent = {}, onSwitchVault = {})
        }
    }
}

@Preview
@Composable
private fun VaultSidebarPreview() {
    SidebarPreviewFrame(state = TreePreviewData.state())
}

@Preview
@Composable
private fun VaultSidebarLoadingPreview() {
    SidebarPreviewFrame(state = TreePreviewData.state().copy(isLoading = true))
}

@Preview
@Composable
private fun VaultSidebarEmptyPreview() {
    SidebarPreviewFrame(state = TreePreviewData.state().copy(rows = emptyList()))
}

@Preview
@Composable
private fun VaultSidebarErrorPreview() {
    SidebarPreviewFrame(
        state = TreePreviewData.state().copy(
            rows = emptyList(),
            loadError = NotesError.PermissionDenied(path = TreePreviewData.VAULT_PATH)
        )
    )
}

@Preview
@Composable
private fun VaultSidebarMessagesPreview() {
    SidebarPreviewFrame(
        state = TreePreviewData.state().copy(
            actionError = NotesError.NameTaken(name = "Arepas.md"),
            notice = TreeNotice.MovedToVaultTrash(name = "Reading list")
        )
    )
}
