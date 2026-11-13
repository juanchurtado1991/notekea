package app.notekea.editor.presentation.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.notes.domain.NotesError
import app.notekea.core.notes.presentation.notesErrorMessage
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.designsystem.components.NotekeaButton
import app.notekea.core.presentation.designsystem.components.NotekeaButtonStyle
import app.notekea.core.presentation.designsystem.components.feedback.ErrorBanner
import app.notekea.core.presentation.designsystem.components.feedback.NotekeaProgressBar
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.editor.domain.CompressError
import app.notekea.editor.presentation.model.EditorIntent
import app.notekea.editor.presentation.model.EditorPreviewData
import app.notekea.editor.presentation.model.EditorState
import app.notekea.editor.presentation.model.VideoCompression
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
import app.notekea.resources.video_compress_cancel
import app.notekea.resources.video_compress_downloading
import app.notekea.resources.video_compress_failed
import app.notekea.resources.video_compress_progress
import app.notekea.resources.video_compress_tool_unavailable
import org.jetbrains.compose.resources.stringResource

private const val PREVIEW_WIDTH_DP = 720

/**
 * The editor's questions and problems, above the note: a note that could not be opened (Try again), an outside change
 * over unsaved edits (Keep mine / Load theirs), a note that disappeared (Restore / Close), a save that failed (the next
 * edit or Save tries again), or a file that could not be attached, opened or located (Close).
 */
@Composable
internal fun EditorBanners(
    state: EditorState,
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit
) {
    Column(
        modifier = Modifier.padding(
            horizontal = NotekeaDimensions.SpacingM,
            vertical = NotekeaDimensions.SpacingXs
        ),
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
                    notesErrorMessage(
                        error = error,
                        platformConventions = platformConventions
                    )
                )
            )
        }

        state.videoCompression?.let { compression ->
            CompressionBanner(
                compression = compression,
                onCancel = { onIntent(EditorIntent.OnVideoCompressionCancel) }
            )
        }

        state.compressError?.let { error ->
            val message = when (error) {
                CompressError.ToolUnavailable -> Res.string.video_compress_tool_unavailable
                CompressError.Failed -> Res.string.video_compress_failed
            }

            ErrorBanner(message = stringResource(resource = message)) {
                BannerAction(text = stringResource(resource = Res.string.editor_close)) {
                    onIntent(EditorIntent.OnCompressErrorDismiss)
                }
            }
        }

        state.actionError?.let { error ->
            ErrorBanner(
                message = notesErrorMessage(
                    error = error,
                    platformConventions = platformConventions
                )
            ) {
                BannerAction(text = stringResource(resource = Res.string.editor_close)) {
                    onIntent(EditorIntent.OnActionErrorDismiss)
                }
            }
        }
    }
}

// A video over GitHub's 100 MB being made smaller, or FFmpeg downloaded for it the first time, with Cancel.
@Composable
private fun CompressionBanner(
    compression: VideoCompression,
    onCancel: () -> Unit
) {
    val label = if (compression.isDownloading) {
        stringResource(resource = Res.string.video_compress_downloading)
    } else {
        stringResource(resource = Res.string.video_compress_progress, compression.name)
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(weight = 1f),
            verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            NotekeaProgressBar(
                label = label,
                fraction = compression.fraction,
                modifier = Modifier.fillMaxWidth()
            )
        }

        BannerAction(
            text = stringResource(resource = Res.string.video_compress_cancel),
            onClick = onCancel
        )
    }
}

@Composable
private fun ConflictBanner(onIntent: (EditorIntent) -> Unit) {
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
private fun DeletedBanner(onIntent: (EditorIntent) -> Unit) {
    ErrorBanner(message = stringResource(resource = Res.string.editor_deleted_message)) {
        BannerAction(text = stringResource(resource = Res.string.editor_close)) {
            onIntent(EditorIntent.OnCloseDeleted)
        }

        BannerAction(text = stringResource(resource = Res.string.editor_restore)) {
            onIntent(EditorIntent.OnRestore)
        }
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
