package app.notekea.shell.presentation.follow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.chat.presentation.viewmodel.ChatViewModel
import app.notekea.clips.presentation.ClipDialog
import app.notekea.clips.presentation.ClipEffect
import app.notekea.clips.presentation.ClipIntent
import app.notekea.clips.presentation.ClipState
import app.notekea.clips.presentation.ClipUpdateDialog
import app.notekea.clips.presentation.ClipViewModel
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.LocalNoteLooks
import app.notekea.core.presentation.designsystem.theme.NoteLooks
import app.notekea.core.presentation.platform.LocalWebSourceUpdater
import app.notekea.editor.presentation.model.EditorIntent
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
        workspace = viewModels.workspace,
        onRewritten = { path ->
            viewModels.editors.forEach {
                it.onIntent(intent = EditorIntent.OnNoteRewritten(path = path))
            }
        }
    )
}

/** The clip dialog over the workspace (4.4), and the clipped note opened in a tab once it is written. */
@Composable
internal fun ClipsOf(
    clip: ClipViewModel,
    workspace: WorkspaceViewModel,
    onRewritten: (RelativePath) -> Unit = {}
) {
    val state by clip.state.collectAsState()

    LaunchedEffect(clip) {
        clip.effects.collect { effect ->
            when (effect) {
                is ClipEffect.Opened ->
                    workspace.onIntent(intent = WorkspaceIntent.OnOpen(path = effect.path))

                // A note an update rewrote is read again by an editor that has it open.
                is ClipEffect.Rewritten -> effect.paths.forEach(onRewritten)
            }
        }
    }

    ClipDialog(
        state = state,
        onIntent = clip::onIntent
    )

    ClipUpdateDialog(
        state = state,
        onIntent = clip::onIntent
    )
}

/**
 * What [content] reads of the open vault wherever it is drawn: how its notes look, and the update of its web sources
 * ([clip]), which the tree's menu offers.
 */
@Composable
internal fun WithVaultLocals(
    looks: NoteLooks,
    clip: ClipViewModel,
    content: @Composable () -> Unit
) {
    val update = { path: RelativePath -> clip.onIntent(intent = ClipIntent.OnUpdate(path = path)) }

    CompositionLocalProvider(
        LocalNoteLooks provides looks,
        LocalWebSourceUpdater provides update,
        content = content
    )
}

@Preview
@Composable
private fun WithVaultLocalsPreview() {
    NotekeaPreviewThemes {
        ClipUpdateDialog(
            state = ClipState(isUpdateShown = true),
            onIntent = {}
        )
    }
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
