package app.notekea.notes.presentation.tree

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.platform.WindowShortcut
import app.notekea.notes.domain.FormatAction
import app.notekea.notes.domain.LinkResolver
import app.notekea.notes.domain.NoteEntry
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.TagRanking
import app.notekea.notes.presentation.editor.EditorIntent
import app.notekea.notes.presentation.editor.EditorViewModel
import app.notekea.notes.presentation.editor.SidePanelHost
import app.notekea.notes.presentation.switcher.QuickSwitcherEffect
import app.notekea.notes.presentation.switcher.QuickSwitcherIntent
import app.notekea.notes.presentation.switcher.QuickSwitcherMode
import app.notekea.notes.presentation.switcher.QuickSwitcherViewModel
import app.notekea.notes.presentation.tags.TagPickerEffect
import app.notekea.notes.presentation.tags.TagPickerIntent
import app.notekea.notes.presentation.tags.TagPickerViewModel
import app.notekea.notes.presentation.templates.TemplatePickerEffect
import app.notekea.notes.presentation.templates.TemplatePickerIntent
import app.notekea.notes.presentation.templates.TemplatePickerViewModel
import app.notekea.notes.presentation.templates.TemplateTarget
import app.notekea.notes.presentation.workspace.WorkspaceEffect
import app.notekea.notes.presentation.workspace.WorkspaceIntent
import app.notekea.notes.presentation.workspace.WorkspaceShortcuts
import app.notekea.resources.Res
import app.notekea.resources.tree_untitled_note
import org.jetbrains.compose.resources.stringResource

/** The window shortcuts of the workspace, and the view models following each other's effects. */
@Composable
internal fun FollowWorkspace(
    state: VaultTreeState,
    viewModels: WorkspaceViewModels,
    sidePanel: SidePanelHost
) {
    val untitledNote = stringResource(resource = Res.string.tree_untitled_note)
    val conventions = state.platformConventions
    val tree = viewModels.tree
    val workspace = viewModels.workspace
    WindowShortcut(shortcut = conventions.newNoteShortcut) {
        tree.onIntent(intent = VaultTreeIntent.OnNewNoteClick(baseName = untitledNote))
    }
    WindowShortcut(shortcut = conventions.toggleSidebarShortcut) {
        tree.onIntent(intent = VaultTreeIntent.OnSidebarToggle)
    }
    DailyNoteShortcuts(platformConventions = conventions, onIntent = tree::onIntent)
    WindowShortcut(shortcut = conventions.bookmarkShortcut) {
        val path = workspace.state.value.workspace.activePath ?: tree.state.value.selectedPath
        path?.let { tree.onIntent(intent = BookmarkIntent.OnToggle(path = it)) }
    }
    QuickSwitcherShortcuts(platformConventions = conventions, onIntent = viewModels.onSwitcher)
    WorkspaceShortcuts(
        platformConventions = conventions,
        onIntent = workspace::onIntent,
        onSidePanelToggle = { sidePanel.onIntent(intent = EditorIntent.OnSidePanelToggle) }
    )
    FollowTree(state = state, viewModels = viewModels)
    LaunchedEffect(workspace) {
        workspace.effects.collect { effect ->
            when (effect) {
                WorkspaceEffect.FocusOutline -> sidePanel.focusOutline()
            }
        }
    }
}

/**
 * The tree and the tabs follow each other: what the tree opens gets a tab, its renames and deletions move or close
 * tabs (and the editors' notes), the tabs learn the vault's files as the tree reads them, and the tree selects the
 * active tab's file. The second pane's editor closes its note when the panes merge.
 */
@Composable
private fun FollowTree(
    state: VaultTreeState,
    viewModels: WorkspaceViewModels
) {
    val viewModel = viewModels.tree
    val workspaceViewModel = viewModels.workspace
    val editors = viewModels.editors
    val workspaceState by workspaceViewModel.state.collectAsState()
    LaunchedEffect(viewModel, workspaceViewModel) {
        viewModel.effects.collect { effect -> followTreeEffect(effect = effect, viewModels = viewModels) }
    }
    LaunchedEffect(state.tree, state.isLoading) {
        workspaceViewModel.onIntent(
            intent = WorkspaceIntent.OnTreeChange(
                tree = state.tree,
                isLoaded =
                !state.isLoading && state.loadError == null
            )
        )
    }
    val activePath = workspaceState.workspace.activePath
    val selected by rememberUpdatedState(newValue = state.selectedPath)
    LaunchedEffect(activePath) {
        if (activePath != null && activePath != selected) {
            viewModel.onIntent(intent = VaultTreeIntent.OnRowSelect(path = activePath))
        }
    }
    val isSplit = workspaceState.workspace.isSplit
    LaunchedEffect(isSplit) {
        if (!isSplit) editors.drop(n = 1).forEach { it.onIntent(EditorIntent.OnOpen(path = null)) }
    }
}

/** What the tree did that the tabs, the editors and the template picker follow. */
private fun followTreeEffect(
    effect: VaultTreeEffect,
    viewModels: WorkspaceViewModels
) {
    val workspace = viewModels.workspace
    when (effect) {
        is VaultTreeEffect.OpenEntry -> {
            workspace.onIntent(intent = WorkspaceIntent.OnOpen(path = effect.path, inBackground = effect.inBackground))
        }

        is VaultTreeEffect.EntryDeleted -> workspace.onIntent(intent = WorkspaceIntent.OnDeleted(path = effect.path))

        is VaultTreeEffect.EntryRenamed -> {
            workspace.onIntent(intent = WorkspaceIntent.OnRenamed(from = effect.from, to = effect.to))
            viewModels.editors.forEach { editor ->
                val open = editor.state.value.notePath ?: return@forEach
                if (open.isWithin(folder = effect.from)) {
                    editor.onIntent(EditorIntent.OnNoteMoved(path = open.rebased(from = effect.from, to = effect.to)))
                }
            }
        }

        is VaultTreeEffect.ChooseTemplate -> viewModels.onTemplatePicker(
            TemplatePickerIntent.OnOpen(target = TemplateTarget.NewNote(folder = effect.folder))
        )
    }
}

/**
 * The palette searches the tree's notes as they change. A note chosen in it opens in a tab of the active pane, and its
 * text takes the keyboard once it opens; closing the palette without choosing gives the keyboard back to the open note.
 */
@Composable
internal fun FollowQuickSwitcher(
    switcher: QuickSwitcherViewModel,
    tree: VaultTreeViewModel,
    editor: EditorViewModel,
    notes: Map<RelativePath, NoteEntry>
) {
    LaunchedEffect(switcher, notes) {
        switcher.onIntent(intent = QuickSwitcherIntent.OnNotesChange(notes = notes.values))
    }
    val currentNotes by rememberUpdatedState(newValue = notes)
    val currentEditor by rememberUpdatedState(newValue = editor)
    LaunchedEffect(switcher, tree) {
        switcher.effects.collect { effect ->
            when (effect) {
                is QuickSwitcherEffect.OpenNote -> {
                    tree.onIntent(intent = VaultTreeIntent.OnRevealNote(path = effect.path))
                    currentEditor.onIntent(intent = EditorIntent.OnFocusRequest(path = effect.path))
                }

                is QuickSwitcherEffect.LinkNote -> {
                    val target = LinkResolver(notePaths = currentNotes.keys).wikiTarget(path = effect.path)
                    currentEditor.onIntent(EditorIntent.OnFormat(action = FormatAction.NoteLink(target = target)))
                }

                QuickSwitcherEffect.Dismissed -> currentEditor.onIntent(intent = EditorIntent.OnFocusRequest())
            }
        }
    }
}

/** `Mod+D` opens today's daily note; `Mod+Alt+←` and `Mod+Alt+→` step to the daily note before or after the open one. */
@Composable
private fun DailyNoteShortcuts(
    platformConventions: PlatformConventions,
    onIntent: (VaultTreeIntent) -> Unit
) {
    WindowShortcut(shortcut = platformConventions.todayShortcut) { onIntent(VaultTreeIntent.OnTodayClick) }
    WindowShortcut(shortcut = platformConventions.previousDayShortcut) {
        onIntent(VaultTreeIntent.OnDayStep(step = -1))
    }
    WindowShortcut(shortcut = platformConventions.nextDayShortcut) { onIntent(VaultTreeIntent.OnDayStep(step = 1)) }
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

/**
 * The tag picker searches the vault's tags as the tree reads them, the most used first. A chosen tag is written in
 * the active pane's note; closing the picker without choosing gives the keyboard back to the note.
 */
@Composable
internal fun FollowTagPicker(
    tagPicker: TagPickerViewModel,
    editor: EditorViewModel,
    notes: Collection<NoteEntry>
) {
    LaunchedEffect(tagPicker, notes) {
        tagPicker.onIntent(intent = TagPickerIntent.OnTagsChange(tags = TagRanking().byUse(notes = notes)))
    }
    val currentEditor by rememberUpdatedState(newValue = editor)
    LaunchedEffect(tagPicker) {
        tagPicker.effects.collect { effect ->
            val intent = when (effect) {
                is TagPickerEffect.TagChosen -> EditorIntent.OnFormat(action = FormatAction.Tag(name = effect.name))
                TagPickerEffect.Dismissed -> EditorIntent.OnFocusRequest()
            }
            currentEditor.onIntent(intent = intent)
        }
    }
}

/**
 * The template picker lists the vault tree's templates as it changes. A template chosen for the open note goes into it
 * at the cursor; one chosen for a new note makes it in the tree, filled once it is named; closing the picker without
 * choosing gives the keyboard back to the note.
 */
@Composable
internal fun FollowTemplatePicker(
    templatePicker: TemplatePickerViewModel,
    tree: VaultTreeViewModel,
    editor: EditorViewModel,
    state: VaultTreeState
) {
    val vaultTree = state.tree
    val untitledNote = stringResource(resource = Res.string.tree_untitled_note)
    LaunchedEffect(templatePicker, vaultTree) {
        templatePicker.onIntent(intent = TemplatePickerIntent.OnTreeChange(tree = vaultTree))
    }
    val currentEditor by rememberUpdatedState(newValue = editor)
    val currentUntitled by rememberUpdatedState(newValue = untitledNote)
    LaunchedEffect(templatePicker, tree) {
        templatePicker.effects.collect { effect ->
            when (effect) {
                is TemplatePickerEffect.TemplateChosen -> when (val target = effect.target) {
                    TemplateTarget.CurrentNote -> {
                        currentEditor.onIntent(intent = EditorIntent.OnTemplateInsert(template = effect.template))
                    }

                    is TemplateTarget.NewNote -> tree.onIntent(
                        intent = VaultTreeIntent.OnNoteFromTemplate(
                            folder = target.folder,
                            template = effect.template,
                            baseName = currentUntitled
                        )
                    )
                }

                TemplatePickerEffect.Dismissed -> currentEditor.onIntent(intent = EditorIntent.OnFocusRequest())
            }
        }
    }
}
