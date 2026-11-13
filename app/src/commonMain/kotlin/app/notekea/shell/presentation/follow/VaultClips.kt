package app.notekea.shell.presentation.follow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.chat.presentation.viewmodel.ChatViewModel
import app.notekea.clips.presentation.ClipDialog
import app.notekea.clips.presentation.ClipEffect
import app.notekea.clips.presentation.ClipState
import app.notekea.clips.presentation.ClipViewModel
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.search.presentation.index.SearchIndexViewModel
import app.notekea.shell.presentation.workspace.PaneEditors
import app.notekea.shell.presentation.workspace.WorkspaceIntent
import app.notekea.shell.presentation.workspace.WorkspaceViewModel
import app.notekea.shell.presentation.workspace.WorkspaceViewModels
import app.notekea.tree.presentation.model.VaultTreeState

/** What the chat and the clip dialog follow while the vault is open: the chat's notes and steps, a clip's note. */
@Composable
internal fun FollowAssistants(
    chat: ChatViewModel,
    clip: ClipViewModel,
    state: VaultTreeState,
    viewModels: WorkspaceViewModels,
    editors: PaneEditors,
    searchIndex: SearchIndexViewModel
) {
    FollowChat(chat, state, viewModels, editors, searchIndex)

    ClipsOf(
        clip = clip,
        workspace = viewModels.workspace
    )
}

/** The clip dialog over the workspace (4.4), and the clipped note opened in a tab once it is written. */
@Composable
internal fun ClipsOf(
    clip: ClipViewModel,
    workspace: WorkspaceViewModel
) {
    val state by clip.state.collectAsState()

    LaunchedEffect(clip) {
        clip.effects.collect { effect ->
            when (effect) {
                is ClipEffect.Opened ->
                    workspace.onIntent(intent = WorkspaceIntent.OnOpen(path = effect.path))
            }
        }
    }

    ClipDialog(
        state = state,
        onIntent = clip::onIntent
    )
}

@Preview
@Composable
private fun ClipsOfPreview() {
    NotekeaPreviewThemes {
        ClipDialog(
            state = ClipState(isOpen = true),
            onIntent = {}
        )
    }
}

@Preview
@Composable
private fun FollowAssistantsPreview() {
    NotekeaPreviewThemes {
        ClipDialog(
            state = ClipState(
                isOpen = true,
                url = "https://example.com/sleep"
            ),
            onIntent = {}
        )
    }
}
