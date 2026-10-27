package app.notekea.notes.presentation.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.components.ErrorBanner
import app.notekea.core.presentation.components.NotekeaButton
import app.notekea.core.presentation.components.NotekeaButtonStyle
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.notes.domain.NotesError
import app.notekea.notes.presentation.tree.notesErrorMessage
import app.notekea.resources.Res
import app.notekea.resources.editor_close
import app.notekea.resources.editor_conflict_message
import app.notekea.resources.editor_deleted_message
import app.notekea.resources.editor_keep_mine
import app.notekea.resources.editor_load_failed
import app.notekea.resources.editor_load_theirs
import app.notekea.resources.editor_restore
import app.notekea.resources.editor_retry
import app.notekea.resources.editor_save_failed
import org.jetbrains.compose.resources.stringResource

private const val PREVIEW_WIDTH_DP = 720

/**
 * The editor's questions and problems, above the note: a note that could not be opened (Try again), an outside change
 * while there were unsaved edits (Keep mine / Load theirs), a note that disappeared (Restore / Close) and a save that
 * failed (the next edit or Save tries again), or a file could not be attached, opened or located (Close). Each one says what happened and what the buttons do.
 */
@Composable
internal fun EditorBanners(
    state: EditorState,
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = NotekeaDimensions.SpacingM, vertical = NotekeaDimensions.SpacingXs),
        verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)
    ) {
        if (state.loadError != null) {
            ErrorBanner(message = stringResource(resource = Res.string.editor_load_failed)) {
                BannerAction(text = stringResource(resource = Res.string.editor_retry)) {
                    onIntent(EditorIntent.OnRetryOpen)
                }
            }
        }
        if (state.hasConflict) ConflictBanner(onIntent = onIntent)
        if (state.isDeleted) DeletedBanner(onIntent = onIntent)
        state.saveError?.let { error ->
            ErrorBanner(
                message = stringResource(
                    Res.string.editor_save_failed,
                    notesErrorMessage(error = error, platformConventions = platformConventions)
                )
            )
        }
        state.actionError?.let { error ->
            ErrorBanner(message = notesErrorMessage(error = error, platformConventions = platformConventions)) {
                BannerAction(text = stringResource(resource = Res.string.editor_close)) {
                    onIntent(EditorIntent.OnActionErrorDismiss)
                }
            }
        }
    }
}

@Composable
private fun ConflictBanner(
    onIntent: (EditorIntent) -> Unit
) {
    ErrorBanner(message = stringResource(resource = Res.string.editor_conflict_message)) {
        BannerAction(text = stringResource(resource = Res.string.editor_load_theirs)) {
            onIntent(EditorIntent.OnLoadTheirs)
        }
        BannerAction(text = stringResource(resource = Res.string.editor_keep_mine)) {
            onIntent(EditorIntent.OnKeepMine)
        }
    }
}

@Composable
private fun DeletedBanner(
    onIntent: (EditorIntent) -> Unit
) {
    ErrorBanner(message = stringResource(resource = Res.string.editor_deleted_message)) {
        BannerAction(text = stringResource(resource = Res.string.editor_close)) {
            onIntent(EditorIntent.OnCloseDeleted)
        }
        BannerAction(text = stringResource(resource = Res.string.editor_restore)) { onIntent(EditorIntent.OnRestore) }
    }
}

@Composable
private fun BannerAction(
    text: String,
    onClick: () -> Unit
) {
    NotekeaButton(
        text = text,
        onClick = onClick,
        modifier = Modifier.padding(start = NotekeaDimensions.SpacingS),
        style = NotekeaButtonStyle.SECONDARY
    )
}

@Preview
@Composable
private fun EditorBannersPreview() {
    NotekeaPreviewThemes {
        Column(modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)) {
            EditorBanners(
                state = EditorPreviewData.state().copy(
                    loadError = NotesError.OperationFailed(path = EditorPreviewData.path.value),
                    hasConflict = true,
                    isDeleted = true,
                    saveError = NotesError.OperationFailed(path = EditorPreviewData.path.value)
                ),
                platformConventions = EditorPreviewData.platformConventions,
                onIntent = {}
            )
        }
    }
}
