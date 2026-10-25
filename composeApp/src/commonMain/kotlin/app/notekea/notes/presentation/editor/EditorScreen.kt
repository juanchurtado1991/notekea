package app.notekea.notes.presentation.editor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.platform.BeforeWindowClose
import app.notekea.core.presentation.platform.WindowShortcut
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.notes.domain.NoteMetadata
import app.notekea.notes.domain.RelativePath
import kotlinx.coroutines.channels.Channel

private const val PREVIEW_HEIGHT_DP = 420
private const val PREVIEW_WIDTH_DP = 720

/**
 * Editor entry point for [notePath]: opens it (saving the previous note first), saves with the save shortcut, saves
 * before the window closes and when the editor leaves the screen (a folder selected, another vault opened), and
 * reports a rename from the title ([onRenamed]) or a deleted note closed ([onClosed]) to the panes around it. The
 * [viewModel] belongs to the vault, so it outlives one note.
 */
@Composable
fun EditorScreen(
    notePath: RelativePath,
    viewModel: EditorViewModel,
    platformConventions: PlatformConventions,
    onRenamed: (EditorEffect.Renamed) -> Unit,
    onClosed: () -> Unit,
    modifier: Modifier = Modifier,
    properties: NoteMetadata? = null
) {
    val state by viewModel.state.collectAsState()
    val flushed = remember(viewModel) { Channel<Unit>(capacity = Channel.CONFLATED) }
    val currentOnRenamed by rememberUpdatedState(newValue = onRenamed)
    val currentOnClosed by rememberUpdatedState(newValue = onClosed)
    LaunchedEffect(notePath) { viewModel.onIntent(intent = EditorIntent.OnOpen(path = notePath)) }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                EditorEffect.Closed -> currentOnClosed()
                EditorEffect.Flushed -> flushed.trySend(element = Unit)
                is EditorEffect.Renamed -> currentOnRenamed(effect)
            }
        }
    }
    DisposableEffect(viewModel) { onDispose { viewModel.onIntent(intent = EditorIntent.OnFlushRequest) } }
    WindowShortcut(shortcut = platformConventions.saveShortcut) {
        viewModel.onIntent(intent = EditorIntent.OnSaveShortcut)
    }
    BeforeWindowClose {
        while (flushed.tryReceive().isSuccess) {
            // A flush from an earlier save is not this one: wait for the save asked for now.
        }
        viewModel.onIntent(intent = EditorIntent.OnFlushRequest)
        flushed.receive()
    }
    EditorContent(
        state = state,
        buffer = viewModel.buffer,
        platformConventions = platformConventions,
        onIntent = viewModel::onIntent,
        modifier = modifier,
        properties = properties
    )
}

@Preview
@Composable
private fun EditorScreenPreview() {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.size(width = PREVIEW_WIDTH_DP.dp, height = PREVIEW_HEIGHT_DP.dp)) {
            EditorContent(
                state = EditorPreviewData.state(),
                buffer = EditorPreviewData.buffer(),
                platformConventions = EditorPreviewData.platformConventions,
                onIntent = {},
                properties = EditorPreviewData.properties
            )
        }
    }
}
