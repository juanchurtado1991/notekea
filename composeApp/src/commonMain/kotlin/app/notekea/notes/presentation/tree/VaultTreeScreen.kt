package app.notekea.notes.presentation.tree

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.platform.WindowShortcut
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.notes.domain.FormatAction
import app.notekea.notes.domain.LinkResolver
import app.notekea.notes.domain.TagRanking
import app.notekea.notes.domain.NoteEntry
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.presentation.editor.EditorIntent
import app.notekea.notes.presentation.editor.EditorScreen
import app.notekea.notes.presentation.editor.EditorViewModel
import app.notekea.notes.presentation.switcher.QuickSwitcherContent
import app.notekea.notes.presentation.switcher.QuickSwitcherEffect
import app.notekea.notes.presentation.switcher.QuickSwitcherIntent
import app.notekea.notes.presentation.switcher.QuickSwitcherMode
import app.notekea.notes.presentation.switcher.QuickSwitcherPurpose
import app.notekea.notes.presentation.switcher.QuickSwitcherViewModel
import app.notekea.resources.Res
import app.notekea.resources.tree_untitled_note
import app.notekea.notes.presentation.tags.TagPickerContent
import app.notekea.notes.presentation.tags.TagPickerEffect
import app.notekea.notes.presentation.tags.TagPickerIntent
import app.notekea.notes.presentation.tags.TagPickerViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Vault workspace entry point for the vault at [vaultPath]: one [VaultTreeViewModel], one [EditorViewModel] and one
 * [QuickSwitcherViewModel] per vault (opening another vault starts fresh), rendered by [VaultTreeContent] with the
 * editor for the selected note and the quick switcher palette over both. They follow each other: a rename in the tree
 * moves the open note in the editor, a rename from the editor's title selects the note at its new path, a note chosen
 * in the palette is selected in the tree (which opens it), a `#tag` clicked in Read mode opens the palette's content
 * search with the tag typed, and the toolbar's Tag opens the tag picker ([TagPickerViewModel]) over the workspace. The new-note, toggle-sidebar, quick switcher and search shortcuts work whatever
 * has focus.
 */
@Composable
fun VaultTreeScreen(
    vaultPath: String,
    vaultName: String,
    onSwitchVault: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VaultTreeViewModel = koinViewModel(key = vaultPath) { parametersOf(vaultPath) },
    editorViewModel: EditorViewModel = koinViewModel(key = EDITOR_KEY_PREFIX + vaultPath) { parametersOf(vaultPath) },
    switcherViewModel: QuickSwitcherViewModel = koinViewModel(
        key = SWITCHER_KEY_PREFIX + vaultPath
    ) { parametersOf(vaultPath) },
    tagPickerViewModel: TagPickerViewModel = koinViewModel(key = TAG_PICKER_KEY_PREFIX + vaultPath)
) {
    val state by viewModel.state.collectAsState()
    val switcherState by switcherViewModel.state.collectAsState()
    val tagPickerState by tagPickerViewModel.state.collectAsState()
    WorkspaceFollowers(
        state = state,
        viewModel = viewModel,
        editorViewModel = editorViewModel,
        switcherViewModel = switcherViewModel
    )
    FollowTagPicker(
        tagPickerViewModel = tagPickerViewModel,
        editorViewModel = editorViewModel,
        notes = state.tree.notes.values
    )
    Box(modifier = modifier) {
        Workspace(
            state = state,
            vaultName = vaultName,
            viewModel = viewModel,
            editorViewModel = editorViewModel,
            onSwitchVault = onSwitchVault,
            onSwitcher = switcherViewModel::onIntent,
            onTagPicker = tagPickerViewModel::onIntent
        )
        QuickSwitcherContent(state = switcherState, onIntent = switcherViewModel::onIntent)
        TagPickerContent(state = tagPickerState, onIntent = tagPickerViewModel::onIntent)
    }
}

/** The window shortcuts of the workspace, and the view models following each other's effects. */
@Composable
private fun WorkspaceFollowers(
    state: VaultTreeState,
    viewModel: VaultTreeViewModel,
    editorViewModel: EditorViewModel,
    switcherViewModel: QuickSwitcherViewModel
) {
    val untitledNote = stringResource(resource = Res.string.tree_untitled_note)
    WindowShortcut(shortcut = state.platformConventions.newNoteShortcut) {
        viewModel.onIntent(intent = VaultTreeIntent.OnNewNoteClick(baseName = untitledNote))
    }
    WindowShortcut(shortcut = state.platformConventions.toggleSidebarShortcut) {
        viewModel.onIntent(intent = VaultTreeIntent.OnSidebarToggle)
    }
    QuickSwitcherShortcuts(platformConventions = state.platformConventions, onIntent = switcherViewModel::onIntent)
    LaunchedEffect(viewModel, editorViewModel) {
        viewModel.effects.collect { effect -> followRename(effect = effect, editorViewModel = editorViewModel) }
    }
    FollowQuickSwitcher(
        switcherViewModel = switcherViewModel,
        viewModel = viewModel,
        editorViewModel = editorViewModel,
        notes = state.tree.notes
    )
}

/**
 * The palette searches the tree's notes as they change. A note chosen in it is selected in the tree, and its text
 * takes the keyboard once it opens; closing the palette without choosing gives the keyboard back to the open note.
 */
@Composable
private fun FollowQuickSwitcher(
    switcherViewModel: QuickSwitcherViewModel,
    viewModel: VaultTreeViewModel,
    editorViewModel: EditorViewModel,
    notes: Map<RelativePath, NoteEntry>
) {
    LaunchedEffect(switcherViewModel, notes) {
        switcherViewModel.onIntent(intent = QuickSwitcherIntent.OnNotesChange(notes = notes.values))
    }
    val currentNotes by rememberUpdatedState(newValue = notes)
    LaunchedEffect(switcherViewModel, viewModel, editorViewModel) {
        switcherViewModel.effects.collect { effect ->
            when (effect) {
                is QuickSwitcherEffect.OpenNote -> {
                    viewModel.onIntent(intent = VaultTreeIntent.OnRevealNote(path = effect.path))
                    editorViewModel.onIntent(intent = EditorIntent.OnFocusRequest(path = effect.path))
                }

                is QuickSwitcherEffect.LinkNote -> {
                    val target = LinkResolver(notePaths = currentNotes.keys).wikiTarget(path = effect.path)
                    editorViewModel.onIntent(
                        intent = EditorIntent.OnFormat(action = FormatAction.NoteLink(target = target))
                    )
                }

                QuickSwitcherEffect.Dismissed -> editorViewModel.onIntent(intent = EditorIntent.OnFocusRequest())
            }
        }
    }
}

/** `Mod+P` opens the palette on note names, `Mod+Shift+F` on note content, whatever has focus. */
@Composable
private fun QuickSwitcherShortcuts(
    platformConventions: PlatformConventions,
    onIntent: (QuickSwitcherIntent) -> Unit
) {
    WindowShortcut(shortcut = platformConventions.quickSwitcherShortcut) {
        onIntent(QuickSwitcherIntent.OnOpen(mode = QuickSwitcherMode.NOTES))
    }
    WindowShortcut(shortcut = platformConventions.searchNotesShortcut) {
        onIntent(QuickSwitcherIntent.OnOpen(mode = QuickSwitcherMode.CONTENT))
    }
}

/** The tree with the editor of the selected note, wired to each other. */
@Composable
private fun Workspace(
    state: VaultTreeState,
    vaultName: String,
    viewModel: VaultTreeViewModel,
    editorViewModel: EditorViewModel,
    onSwitchVault: () -> Unit,
    onSwitcher: (QuickSwitcherIntent) -> Unit,
    onTagPicker: (TagPickerIntent) -> Unit
) {
    VaultTreeContent(
        state = state,
        vaultName = vaultName,
        onIntent = viewModel::onIntent,
        onSwitchVault = onSwitchVault,
        onSearch = { onSwitcher(QuickSwitcherIntent.OnOpen(mode = QuickSwitcherMode.NOTES)) },
        noteContent = { note ->
            NoteEditor(
                note = note,
                state = state,
                viewModel = viewModel,
                editorViewModel = editorViewModel,
                onSwitcher = onSwitcher,
                onTagPicker = onTagPicker
            )
        }
    )
}

/**
 * The editor of the selected note, following the tree: a rename or a closed note is told to the tree, a link opens
 * its note there, a clicked `#tag` is searched, and the toolbar's note link and tag open their pickers.
 */
@Composable
private fun NoteEditor(
    note: NoteEntry,
    state: VaultTreeState,
    viewModel: VaultTreeViewModel,
    editorViewModel: EditorViewModel,
    onSwitcher: (QuickSwitcherIntent) -> Unit,
    onTagPicker: (TagPickerIntent) -> Unit
) {
    EditorScreen(
        notePath = note.file.path,
        viewModel = editorViewModel,
        platformConventions = state.platformConventions,
        onRenamed = {
            viewModel.onIntent(intent = VaultTreeIntent.OnEntryRenamed(from = it.from, to = it.to, links = it.links))
        },
        onClosed = { viewModel.onIntent(intent = VaultTreeIntent.OnSelectionClear) },
        onOpenLink = {
            viewModel.onIntent(
                intent = VaultTreeIntent.OnFollowLink(source = it.source, target = it.target, kind = it.kind)
            )
        },
        onSearchTag = { tag ->
            onSwitcher(QuickSwitcherIntent.OnOpen(mode = QuickSwitcherMode.CONTENT, query = TAG_MARK + tag))
        },
        onRequestNoteLink = {
            onSwitcher(QuickSwitcherIntent.OnOpen(mode = QuickSwitcherMode.NOTES, purpose = QuickSwitcherPurpose.LINK))
        },
        onRequestTag = { query -> onTagPicker(TagPickerIntent.OnOpen(query = query)) },
        properties = note.metadata
    )
}

/**
 * The tag picker searches the vault's tags as the tree reads them, the most used first. A chosen tag is written in
 * the open note; closing the picker without choosing gives the keyboard back to the note.
 */
@Composable
private fun FollowTagPicker(
    tagPickerViewModel: TagPickerViewModel,
    editorViewModel: EditorViewModel,
    notes: Collection<NoteEntry>
) {
    LaunchedEffect(tagPickerViewModel, notes) {
        tagPickerViewModel.onIntent(intent = TagPickerIntent.OnTagsChange(tags = TagRanking().byUse(notes = notes)))
    }
    LaunchedEffect(tagPickerViewModel, editorViewModel) {
        tagPickerViewModel.effects.collect { effect ->
            val intent = when (effect) {
                is TagPickerEffect.TagChosen -> EditorIntent.OnFormat(action = FormatAction.Tag(name = effect.name))
                TagPickerEffect.Dismissed -> EditorIntent.OnFocusRequest()
            }
            editorViewModel.onIntent(intent = intent)
        }
    }
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
private const val SWITCHER_KEY_PREFIX = "switcher:"
private const val TAG_PICKER_KEY_PREFIX = "tags:"
private const val TAG_MARK = "#"

@Preview
@Composable
private fun VaultTreeScreenPreview() {
    NotekeaPreviewThemes {
        VaultTreeContent(
            state = TreePreviewData.state(),
            vaultName = TreePreviewData.VAULT_NAME,
            onIntent = {},
            onSwitchVault = {},
            onSearch = {},
            noteContent = { PreviewNoteContent(note = it) }
        )
    }
}
