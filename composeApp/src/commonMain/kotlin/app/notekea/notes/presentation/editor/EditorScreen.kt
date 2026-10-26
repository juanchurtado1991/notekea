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
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
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
 * Editor entry point for [notePath]: opens it (saving the previous note first), opens Find and switches Edit / Read
 * with their shortcuts, saves
 * with the save shortcut, saves
 * before the window closes and when the editor leaves the screen (a folder selected, another vault opened), and
 * reports a rename from the title ([onRenamed]), a deleted note closed ([onClosed]) or a link clicked in Read mode
 * ([onOpenLink]) to the panes around it. The
 * [viewModel] belongs to the vault, so it outlives one note.
 */
@Composable
fun EditorScreen(
    notePath: RelativePath,
    viewModel: EditorViewModel,
    platformConventions: PlatformConventions,
    onRenamed: (EditorEffect.Renamed) -> Unit,
    onClosed: () -> Unit,
    onOpenLink: (EditorEffect.OpenLink) -> Unit,
    onSearchTag: (String) -> Unit,
    onRequestNoteLink: () -> Unit,
    onRequestTag: (String) -> Unit,
    modifier: Modifier = Modifier,
    properties: NoteMetadata? = null
) {
    val state by viewModel.state.collectAsState()
    val flushed = remember(viewModel) { Channel<Unit>(capacity = Channel.CONFLATED) }
    val textFocusRequester = remember { FocusRequester() }
    LaunchedEffect(notePath) { viewModel.onIntent(intent = EditorIntent.OnOpen(path = notePath)) }
    EditorEffects(
        viewModel = viewModel,
        flushed = flushed,
        textFocusRequester = textFocusRequester,
        onRenamed = onRenamed,
        onClosed = onClosed,
        onOpenLink = onOpenLink,
        onSearchTag = onSearchTag,
        onRequestNoteLink = onRequestNoteLink,
        onRequestTag = onRequestTag
    )
    DisposableEffect(viewModel) { onDispose { viewModel.onIntent(intent = EditorIntent.OnFlushRequest) } }
    EditorShortcuts(platformConventions = platformConventions, onIntent = viewModel::onIntent)
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
        properties = properties,
        textFocusRequester = textFocusRequester
    )
}

/** What the editor asks of the window around it: the callbacks, the end of a flush, and the keyboard for its text. */
@Composable
private fun EditorEffects(
    viewModel: EditorViewModel,
    flushed: Channel<Unit>,
    textFocusRequester: FocusRequester,
    onRenamed: (EditorEffect.Renamed) -> Unit,
    onClosed: () -> Unit,
    onOpenLink: (EditorEffect.OpenLink) -> Unit,
    onSearchTag: (String) -> Unit,
    onRequestNoteLink: () -> Unit,
    onRequestTag: (String) -> Unit
) {
    val currentOnRequestNoteLink by rememberUpdatedState(newValue = onRequestNoteLink)
    val currentOnRequestTag by rememberUpdatedState(newValue = onRequestTag)
    val currentOnRenamed by rememberUpdatedState(newValue = onRenamed)
    val currentOnClosed by rememberUpdatedState(newValue = onClosed)
    val currentOnOpenLink by rememberUpdatedState(newValue = onOpenLink)
    val currentOnSearchTag by rememberUpdatedState(newValue = onSearchTag)
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                EditorEffect.Closed -> currentOnClosed()

                EditorEffect.Flushed -> flushed.trySend(element = Unit)

                is EditorEffect.OpenLink -> currentOnOpenLink(effect)

                is EditorEffect.Renamed -> currentOnRenamed(effect)

                is EditorEffect.SearchTag -> currentOnSearchTag(effect.tag)

                EditorEffect.RequestNoteLink -> currentOnRequestNoteLink()

                is EditorEffect.RequestTag -> currentOnRequestTag(effect.query)

                EditorEffect.FocusText -> focusText(
                    requester = textFocusRequester,
                    isReading = viewModel.state.value.isReading
                )
            }
        }
    }
}

/**
 * Gives the keyboard to the note's text one frame later, once the text field that replaced the loading skeleton is
 * laid out. Read mode has no cursor, so nothing takes focus there.
 */
private suspend fun focusText(
    requester: FocusRequester,
    isReading: Boolean
) {
    if (isReading) return
    withFrameNanos { }
    try {
        requester.requestFocus()
    } catch (_: IllegalStateException) {
        // The text is not shown (the note failed to open or was closed meanwhile): there is nothing to focus.
    }
}

/** Save, Find in note, Edit / Read and toggling a task work whatever has focus while the editor is shown. */
@Composable
private fun EditorShortcuts(
    platformConventions: PlatformConventions,
    onIntent: (EditorIntent) -> Unit
) {
    WindowShortcut(shortcut = platformConventions.saveShortcut) { onIntent(EditorIntent.OnSaveShortcut) }
    WindowShortcut(shortcut = platformConventions.findShortcut) { onIntent(EditorIntent.OnFindOpen) }
    WindowShortcut(shortcut = platformConventions.toggleReadModeShortcut) { onIntent(EditorIntent.OnReadModeToggle) }
    WindowShortcut(shortcut = platformConventions.toggleTaskShortcut) { onIntent(EditorIntent.OnTaskToggleShortcut) }
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
