package app.notekea.shell.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.platform.WindowShortcut
import app.notekea.editor.domain.FormatAction
import app.notekea.core.notes.domain.LinkResolver
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.RelativePath
import app.notekea.tree.domain.TagRanking
import app.notekea.editor.presentation.model.EditorIntent
import app.notekea.editor.presentation.viewmodel.EditorViewModel
import app.notekea.editor.presentation.sidepanel.SidePanelHost
import app.notekea.search.presentation.switcher.QuickSwitcherEffect
import app.notekea.search.presentation.switcher.QuickSwitcherIntent
import app.notekea.search.presentation.switcher.QuickSwitcherMode
import app.notekea.search.presentation.switcher.QuickSwitcherViewModel
import app.notekea.tree.presentation.tags.TagPickerEffect
import app.notekea.tree.presentation.tags.TagPickerIntent
import app.notekea.tree.presentation.tags.TagPickerViewModel
import app.notekea.templates.presentation.TemplatePickerEffect
import app.notekea.templates.presentation.TemplatePickerIntent
import app.notekea.templates.presentation.TemplatePickerViewModel
import app.notekea.templates.presentation.TemplateTarget
import app.notekea.shell.presentation.WorkspaceEffect
import app.notekea.shell.presentation.WorkspaceIntent
import app.notekea.shell.presentation.WorkspaceShortcuts
import app.notekea.resources.Res
import app.notekea.resources.tree_untitled_note
import org.jetbrains.compose.resources.stringResource
import app.notekea.tree.presentation.model.BookmarkIntent
import app.notekea.tree.presentation.model.VaultTreeEffect
import app.notekea.tree.presentation.model.VaultTreeIntent
import app.notekea.tree.presentation.model.VaultTreeState
import app.notekea.tree.presentation.viewmodel.VaultTreeViewModel
import app.notekea.shell.domain.Workspace

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
        val active = workspace.state.value.workspace.activePath?.takeIf { it !in Workspace.TOOL_TABS }
        val path = active ?: tree.state.value.selectedPath
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
                WorkspaceEffect.UnexpectedError -> viewModels.tree.onIntent(intent = VaultTreeIntent.OnUnexpectedError)
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
        if (activePath != null && activePath != selected && activePath !in Workspace.TOOL_TABS) {
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

        is VaultTreeEffect.LinksRewritten -> viewModels.editors.forEach { editor ->
            val open = editor.state.value.notePath ?: return@forEach
            if (open in effect.notes) editor.onIntent(EditorIntent.OnNoteRewritten(path = open))
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
    editors: PaneEditors,
    notes: Map<RelativePath, NoteEntry>
) {
    LaunchedEffect(switcher, notes) {
        switcher.onIntent(intent = QuickSwitcherIntent.OnNotesChange(notes = notes.values))
    }
    val currentNotes by rememberUpdatedState(newValue = notes)
    val currentEditors by rememberUpdatedState(newValue = editors)
    LaunchedEffect(switcher, tree) {
        switcher.effects.collect { effect ->
            followSwitcherEffect(
                effect = effect,
                onReveal = { tree.onIntent(intent = VaultTreeIntent.OnRevealNote(path = it)) },
                editors = currentEditors,
                notes = currentNotes.keys
            )
        }
    }
}

/**
 * What the palette chose. The tree opens a chosen note a moment later, so its keyboard request goes to the editor of
 * the pane it will show in, which keeps it until the note is there.
 */
internal fun followSwitcherEffect(
    effect: QuickSwitcherEffect,
    onReveal: (RelativePath) -> Unit,
    editors: PaneEditors,
    notes: Set<RelativePath>
) {
    when (effect) {
        is QuickSwitcherEffect.OpenNote -> {
            onReveal(effect.path)
            val editor = editors.of(path = effect.path)
            val passage = effect.passage
            editor.onIntent(
                intent = if (passage == null) {
                    EditorIntent.OnFocusRequest(path = effect.path)
                } else {
                    EditorIntent.OnRevealRequest(path = effect.path, range = passage)
                }
            )
        }

        is QuickSwitcherEffect.LinkNote -> {
            val target = LinkResolver(notePaths = notes).wikiTarget(path = effect.path)
            editors.active.onIntent(EditorIntent.OnFormat(action = FormatAction.NoteLink(target = target)))
        }

        QuickSwitcherEffect.Dismissed -> editors.active.onIntent(intent = EditorIntent.OnFocusRequest())
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
