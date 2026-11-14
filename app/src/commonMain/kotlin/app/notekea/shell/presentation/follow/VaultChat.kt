package app.notekea.shell.presentation.follow

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.ai.domain.InstallProgress
import app.notekea.ai.domain.InstallSession
import app.notekea.ai.domain.ModelSize
import app.notekea.ai.presentation.ModelDownloadEffect
import app.notekea.ai.presentation.ModelDownloadState
import app.notekea.ai.presentation.ModelDownloadStatus
import app.notekea.ai.presentation.ModelDownloadViewModel
import app.notekea.chat.presentation.ChatPanel
import app.notekea.chat.presentation.model.ChatEffect
import app.notekea.chat.presentation.model.ChatIntent
import app.notekea.chat.presentation.model.ChatPreviewData
import app.notekea.chat.presentation.viewmodel.ChatViewModel
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.platform.LocalWindowDialogs
import app.notekea.core.presentation.platform.WindowDialog
import app.notekea.search.presentation.index.SearchIndexState
import app.notekea.search.presentation.index.SearchIndexStatus
import app.notekea.search.presentation.index.SearchIndexViewModel
import app.notekea.shell.presentation.workspace.PaneEditors
import app.notekea.shell.presentation.workspace.WorkspaceIntent
import app.notekea.shell.presentation.workspace.WorkspaceViewModels
import app.notekea.tree.presentation.model.VaultTreeState

private const val PREVIEW_WIDTH_DP = 320
private const val PREVIEW_HEIGHT_DP = 560
private const val PREVIEW_DONE_BYTES = 420L
private const val PREVIEW_TOTAL_BYTES = 1_000L
private const val PREVIEW_READY_NOTES = 40

/** The side panel's chat: the vault's [chat], its question field taking the keyboard from [inputFocus] (`Mod+L`). */
@Composable
internal fun VaultChatPanel(
    chat: ChatViewModel,
    inputFocus: FocusRequester
) {
    val state by chat.state.collectAsState()

    ChatPanel(
        state = state,
        onIntent = chat::onIntent,
        inputFocus = inputFocus
    )
}

/**
 * The chat follows the vault: the [tree]'s notes, the index's runs ([searchIndex]) and the active pane's note, for its
 * scopes; a source opens its note at the passage ([editors]), and "Choose a model" opens Settings at AI model.
 */
@Composable
internal fun FollowChat(
    chat: ChatViewModel,
    tree: VaultTreeState,
    viewModels: WorkspaceViewModels,
    editors: PaneEditors,
    searchIndex: SearchIndexViewModel
) {
    // The window's view models are built again on each change of the tabs: the effects read the latest ones.
    val currentViewModels by rememberUpdatedState(newValue = viewModels)
    val workspace = viewModels.workspace
    val tabs by workspace.state.collectAsState()
    val indexState by searchIndex.state.collectAsState()
    val activeEditor = remember(editors, tabs.workspace.activePane) { editors.active }
    val editorState by activeEditor.state.collectAsState()
    val dialogs = LocalWindowDialogs.current
    val currentNotes by rememberUpdatedState(newValue = tree.tree.notes.keys)
    val isIndexReady = !indexState.isRunning && indexState.readyNotes != null

    LaunchedEffect(chat, tree.tree.notes, tree.tree.attachments) {
        chat.onIntent(intent = ChatIntent.OnNotesChange(notes = tree.tree.readable))
    }

    LaunchedEffect(chat, isIndexReady) {
        chat.onIntent(intent = ChatIntent.OnIndexChange(isReady = isIndexReady))
    }

    val activePath = tabs.workspace.activePath

    // The note of the tab in front, and none when it is a PDF, a view or nothing at all: the editor still holds
    // the last note it showed, which the chat kept asking about (the code review of 2026-11-13).
    val activeNote = editorState.notePath?.takeIf {
        it == activePath
    }

    LaunchedEffect(chat, activeNote) {
        chat.onIntent(intent = ChatIntent.OnActiveNoteChange(path = activeNote))
    }

    FollowWork(
        chat = chat,
        editor = activeEditor,
        outline = editorState.outline,
        kind = activePath?.let { tabs.kindOf(path = it) },
        path = activePath,
        dialog = dialogs.shown
    )

    LaunchedEffect(chat, editors) {
        val actions = connectionActions(
            workspace = workspace,
            editors = editors,
            notes = { currentNotes }
        )

        chat.effects.collect { effect ->
            when (effect) {
                is ChatEffect.Reveal -> actions.onShow(effect.note, effect.range)

                ChatEffect.OpenModelSettings -> dialogs.shown = WindowDialog.AI_MODEL

                is ChatEffect.Open -> currentViewModels.workspace.onIntent(
                    intent = WorkspaceIntent.OnOpen(path = effect.note)
                )

                is ChatEffect.Show -> showStep(
                    step = effect.step,
                    viewModels = currentViewModels,
                    editors = editors
                )
            }
        }
    }
}

/**
 * The status bar's AI entries: the chat model's installation while one runs, a click opening Settings → AI model, and
 * the search index's status.
 */
@Composable
internal fun AiStatusOf(
    searchIndex: SearchIndexViewModel,
    modelDownload: ModelDownloadViewModel
) {
    val indexState by searchIndex.state.collectAsState()
    val downloadState by modelDownload.state.collectAsState()
    val dialogs = LocalWindowDialogs.current

    LaunchedEffect(modelDownload) {
        modelDownload.effects.collect { effect ->
            when (effect) {
                ModelDownloadEffect.OpenSettings -> dialogs.shown = WindowDialog.AI_MODEL
            }
        }
    }

    ModelDownloadStatus(
        state = downloadState,
        onIntent = modelDownload::onIntent
    )

    SearchIndexStatus(
        state = indexState,
        onIntent = searchIndex::onIntent
    )
}

@Preview
@Composable
private fun VaultChatPanelPreview() {
    NotekeaPreviewThemes {
        Box(
            modifier = Modifier.size(
                width = PREVIEW_WIDTH_DP.dp,
                height = PREVIEW_HEIGHT_DP.dp
            )
        ) {
            ChatPanel(
                state = ChatPreviewData.state,
                onIntent = {},
                inputFocus = remember { FocusRequester() }
            )
        }
    }
}

@Preview
@Composable
private fun AiStatusOfPreview() {
    NotekeaPreviewThemes {
        Row {
            ModelDownloadStatus(
                state = ModelDownloadState(
                    session = InstallSession(
                        size = ModelSize.MEDIUM,
                        progress = InstallProgress.Downloading(
                            doneBytes = PREVIEW_DONE_BYTES,
                            totalBytes = PREVIEW_TOTAL_BYTES
                        )
                    )
                ),
                onIntent = {}
            )

            SearchIndexStatus(
                state = SearchIndexState(readyNotes = PREVIEW_READY_NOTES),
                onIntent = {}
            )
        }
    }
}
