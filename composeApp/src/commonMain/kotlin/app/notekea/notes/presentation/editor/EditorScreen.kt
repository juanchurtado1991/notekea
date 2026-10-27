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
import app.notekea.notes.domain.DayNeighbors
import app.notekea.notes.domain.NoteMetadata
import app.notekea.notes.domain.OutlineHeading
import app.notekea.notes.domain.RelativePath
import kotlinx.coroutines.channels.Channel

private const val PREVIEW_HEIGHT_DP = 420
private const val PREVIEW_WIDTH_DP = 720

/**
 * Editor entry point for [notePath] in one pane: opens it (saving the previous note first), saves before the window
 * closes and when the editor leaves the screen (a folder selected, another vault opened), and reports a rename from the
 * title ([onRenamed]), a deleted note closed ([onClosed]) or a link clicked in Read mode ([onOpenLink]) to the panes
 * around it. Find, Edit / Read and the other shortcuts work in the pane the keyboard is in ([isActive]). The
 * [viewModel] belongs to the vault's pane, so it outlives one note. The outline beside the panes follows this pane
 * through [tracker]; the side panel's requests go to [sidePanel]; [scrollMemory] keeps where each note was.
 */
@Composable
fun EditorScreen(
    notePath: RelativePath,
    viewModel: EditorViewModel,
    platformConventions: PlatformConventions,
    callbacks: EditorCallbacks,
    pane: EditorPaneTools,
    sidePanel: SidePanelHost,
    modifier: Modifier = Modifier,
    isActive: Boolean = true,
    properties: NoteMetadata? = null,
    dayNeighbors: DayNeighbors? = null
) {
    val state by viewModel.state.collectAsState()
    val flushed = remember(viewModel) { Channel<Unit>(capacity = Channel.CONFLATED) }
    val textFocusRequester = remember { FocusRequester() }
    val onIntent = remember(viewModel, sidePanel) { routerOf(viewModel = viewModel, sidePanel = sidePanel) }
    LaunchedEffect(notePath) { viewModel.onIntent(intent = EditorIntent.OnOpen(path = notePath)) }
    EditorEffects(
        viewModel = viewModel,
        flushed = flushed,
        textFocusRequester = textFocusRequester,
        callbacks = callbacks,
        onRevealHeading = { heading ->
            sidePanel.isOverlayOpen = false
            pane.tracker.reveal(heading = heading)
        }
    )
    DisposableEffect(viewModel) { onDispose { viewModel.onIntent(intent = EditorIntent.OnFlushRequest) } }
    if (isActive) EditorShortcuts(platformConventions = platformConventions, onIntent = onIntent)
    FlushBeforeWindowClose(viewModel = viewModel, flushed = flushed)
    EditorContent(
        state = state,
        buffer = viewModel.buffer,
        platformConventions = platformConventions,
        onIntent = onIntent,
        modifier = modifier,
        properties = properties,
        textFocusRequester = textFocusRequester,
        outlineTracker = pane.tracker,
        dayNeighbors = dayNeighbors,
        scrollMemory = pane.scrollMemory
    )
}

/** The editor's intents to [viewModel], except the side panel's requests, which go to [sidePanel]. */
private fun routerOf(
    viewModel: EditorViewModel,
    sidePanel: SidePanelHost
): (EditorIntent) -> Unit = { intent ->
    if (SidePanelHost.isRequest(intent = intent)) sidePanel.onIntent(intent = intent) else viewModel.onIntent(intent)
}

/** Saves the note before the window closes, waiting for that save ([flushed]) rather than one asked for earlier. */
@Composable
private fun FlushBeforeWindowClose(
    viewModel: EditorViewModel,
    flushed: Channel<Unit>
) {
    BeforeWindowClose {
        while (flushed.tryReceive().isSuccess) {
            // A flush from an earlier save is not this one: wait for the save asked for now.
        }
        viewModel.onIntent(intent = EditorIntent.OnFlushRequest)
        flushed.receive()
    }
}

/** What the editor asks of the window around it: the callbacks, the end of a flush, and the keyboard for its text. */
@Composable
private fun EditorEffects(
    viewModel: EditorViewModel,
    flushed: Channel<Unit>,
    textFocusRequester: FocusRequester,
    callbacks: EditorCallbacks,
    onRevealHeading: (OutlineHeading) -> Unit
) {
    val current by rememberUpdatedState(newValue = callbacks)
    val currentOnRevealHeading by rememberUpdatedState(newValue = onRevealHeading)
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                EditorEffect.Closed -> current.onClosed()

                EditorEffect.Flushed -> flushed.trySend(element = Unit)

                is EditorEffect.OpenLink -> current.onOpenLink(effect)

                is EditorEffect.Renamed -> current.onRenamed(effect)

                is EditorEffect.SearchTag -> current.onSearchTag(effect.tag)

                EditorEffect.RequestNoteLink -> current.onRequestNoteLink()

                is EditorEffect.RequestTag -> current.onRequestTag(effect.query)

                EditorEffect.FocusText -> focusText(
                    requester = textFocusRequester,
                    isReading = viewModel.state.value.isReading
                )

                is EditorEffect.RevealHeading -> currentOnRevealHeading(effect.heading)

                is EditorEffect.DayStep -> current.onDayStep(effect.step)
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

/** Save, Find in note, Edit / Read and toggling a task work whatever has focus while the editor's pane is active. */
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
