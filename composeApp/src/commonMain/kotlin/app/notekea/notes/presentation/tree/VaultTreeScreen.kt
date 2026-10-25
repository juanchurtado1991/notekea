package app.notekea.notes.presentation.tree

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.platform.WindowShortcut
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.notes.presentation.editor.EditorIntent
import app.notekea.notes.presentation.editor.EditorScreen
import app.notekea.notes.presentation.editor.EditorViewModel
import app.notekea.resources.Res
import app.notekea.resources.tree_untitled_note
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Vault workspace entry point for the vault at [vaultPath]: one [VaultTreeViewModel] and one [EditorViewModel] per
 * vault (opening another vault starts fresh), rendered by [VaultTreeContent] with the editor for the selected note.
 * The two follow each other: a rename in the tree moves the open note in the editor, and a rename from the editor's
 * title selects the note at its new path. The new-note and toggle-sidebar shortcuts work whatever has focus.
 */
@Composable
fun VaultTreeScreen(
    vaultPath: String,
    vaultName: String,
    onSwitchVault: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VaultTreeViewModel = koinViewModel(key = vaultPath) { parametersOf(vaultPath) },
    editorViewModel: EditorViewModel = koinViewModel(key = EDITOR_KEY_PREFIX + vaultPath) { parametersOf(vaultPath) }
) {
    val state by viewModel.state.collectAsState()
    val untitledNote = stringResource(resource = Res.string.tree_untitled_note)
    WindowShortcut(shortcut = state.platformConventions.newNoteShortcut) {
        viewModel.onIntent(intent = VaultTreeIntent.OnNewNoteClick(baseName = untitledNote))
    }
    WindowShortcut(shortcut = state.platformConventions.toggleSidebarShortcut) {
        viewModel.onIntent(intent = VaultTreeIntent.OnSidebarToggle)
    }
    LaunchedEffect(viewModel, editorViewModel) {
        viewModel.effects.collect { effect -> followRename(effect = effect, editorViewModel = editorViewModel) }
    }
    VaultTreeContent(
        state = state,
        vaultName = vaultName,
        onIntent = viewModel::onIntent,
        onSwitchVault = onSwitchVault,
        noteContent = { note ->
            EditorScreen(
                notePath = note.file.path,
                viewModel = editorViewModel,
                platformConventions = state.platformConventions,
                onRenamed = {
                    viewModel.onIntent(
                        intent = VaultTreeIntent.OnEntryRenamed(from = it.from, to = it.to, links = it.links)
                    )
                },
                onClosed = { viewModel.onIntent(intent = VaultTreeIntent.OnSelectionClear) },
                properties = note.metadata
            )
        },
        modifier = modifier
    )
}

/** A note (or a folder holding it) renamed in the tree: the editor keeps the open note at its new path. */
private fun followRename(
    effect: VaultTreeEffect,
    editorViewModel: EditorViewModel
) {
    when (effect) {
        is VaultTreeEffect.EntryRenamed -> {
            val open = editorViewModel.state.value.notePath ?: return
            if (open.isWithin(folder = effect.from)) {
                editorViewModel.onIntent(
                    intent = EditorIntent.OnNoteMoved(path = open.rebased(from = effect.from, to = effect.to))
                )
            }
        }
    }
}

private const val EDITOR_KEY_PREFIX = "editor:"

@Preview
@Composable
private fun VaultTreeScreenPreview() {
    NotekeaPreviewThemes {
        VaultTreeContent(
            state = TreePreviewData.state(),
            vaultName = TreePreviewData.VAULT_NAME,
            onIntent = {},
            onSwitchVault = {},
            noteContent = { PreviewNoteContent(note = it) }
        )
    }
}
