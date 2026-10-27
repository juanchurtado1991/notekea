package app.notekea.notes.presentation.tree

import app.notekea.notes.domain.VaultTree
import app.notekea.notes.presentation.templates.TemplatePickerEffect
import app.notekea.notes.presentation.workspace.TabKind
import app.notekea.notes.presentation.workspace.WorkspaceState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.platform.WindowShortcut
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.notes.domain.FormatAction
import app.notekea.notes.domain.LinkResolver
import app.notekea.notes.domain.NoteEntry
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.TagRanking
import app.notekea.notes.domain.Workspace
import app.notekea.notes.presentation.editor.EditorIntent
import app.notekea.notes.presentation.editor.EditorPaneTools
import app.notekea.notes.presentation.editor.EditorViewModel
import app.notekea.notes.presentation.editor.SidePanelHost
import app.notekea.notes.presentation.switcher.QuickSwitcherContent
import app.notekea.notes.presentation.switcher.QuickSwitcherEffect
import app.notekea.notes.presentation.switcher.QuickSwitcherIntent
import app.notekea.notes.presentation.switcher.QuickSwitcherMode
import app.notekea.notes.presentation.switcher.QuickSwitcherViewModel
import app.notekea.notes.presentation.tags.TagPickerContent
import app.notekea.notes.presentation.tags.TagPickerEffect
import app.notekea.notes.presentation.tags.TagPickerIntent
import app.notekea.notes.presentation.tags.TagPickerViewModel
import app.notekea.notes.presentation.templates.TemplatePickerContent
import app.notekea.notes.presentation.templates.TemplatePickerIntent
import app.notekea.notes.presentation.templates.TemplatePickerViewModel
import app.notekea.notes.presentation.templates.TemplateTarget
import app.notekea.notes.presentation.workspace.WorkspaceEffect
import app.notekea.notes.presentation.workspace.WorkspaceIntent
import app.notekea.notes.presentation.workspace.WorkspaceShortcuts
import app.notekea.notes.presentation.workspace.WorkspaceViewModel
import app.notekea.resources.Res
import app.notekea.resources.tree_untitled_note
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Vault workspace entry point for the vault at [vaultPath]: one [VaultTreeViewModel], one [WorkspaceViewModel] for the
 * tabs and panes, one [EditorViewModel] per pane and one [QuickSwitcherViewModel] per vault (opening another vault
 * starts fresh), rendered by [VaultWorkspace] with the quick switcher palette, the tag picker and the template picker
 * over it. They follow each other: what the tree opens (a click, a link, the quick switcher, Today) opens in a tab, and
 * the tree selects the active tab's file; a rename moves the tabs and the editors' notes, a deletion closes its tabs;
 * the palette and the pickers work on the note of the active pane. The workspace shortcuts work whatever has focus.
 */
@Composable
fun VaultTreeScreen(
    vaultPath: String,
    vaultName: String,
    onSwitchVault: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VaultTreeViewModel = koinViewModel(key = vaultPath) { parametersOf(vaultPath) },
    workspaceViewModel: WorkspaceViewModel = koinViewModel(
        key = WORKSPACE_KEY_PREFIX + vaultPath
    ) { parametersOf(vaultPath) },
    editorViewModels: List<EditorViewModel> = List(size = Workspace.MAX_PANES) { pane ->
        koinViewModel(key = EDITOR_KEY_PREFIX + pane + vaultPath) { parametersOf(vaultPath) }
    },
    switcherViewModel: QuickSwitcherViewModel = koinViewModel(
        key = SWITCHER_KEY_PREFIX + vaultPath
    ) { parametersOf(vaultPath) },
    tagPickerViewModel: TagPickerViewModel = koinViewModel(key = TAG_PICKER_KEY_PREFIX + vaultPath),
    templatePickerViewModel: TemplatePickerViewModel = koinViewModel(
        key = TEMPLATE_PICKER_KEY_PREFIX + vaultPath
    ) { parametersOf(vaultPath) }
) {
    val state by viewModel.state.collectAsState()
    val workspaceState by workspaceViewModel.state.collectAsState()
    val activeEditor = editorViewModels[workspaceState.workspace.activePane]
    val panes = remember(vaultPath) { List(size = Workspace.MAX_PANES) { EditorPaneTools() } }
    val sidePanel = remember(workspaceViewModel) {
        SidePanelHost(send = { workspaceViewModel.onIntent(intent = workspaceIntentOf(intent = it)) })
    }
    val viewModels = WorkspaceViewModels(
        tree = viewModel,
        workspace = workspaceViewModel,
        editors = editorViewModels,
        onSwitcher = switcherViewModel::onIntent,
        onTagPicker = tagPickerViewModel::onIntent,
        onTemplatePicker = templatePickerViewModel::onIntent
    )
    WorkspaceFollowers(state = state, viewModels = viewModels, sidePanel = sidePanel)
    Box(modifier = modifier) {
        VaultWorkspace(
            state = state,
            workspaceState = workspaceState,
            vaultName = vaultName,
            viewModels = viewModels,
            panes = panes,
            sidePanel = sidePanel,
            onSwitchVault = onSwitchVault
        )
        Pickers(
            state = state,
            workspaceState = workspaceState,
            tree = viewModel,
            editor = activeEditor,
            switcher = switcherViewModel,
            tagPicker = tagPickerViewModel,
            templatePicker = templatePickerViewModel
        )
    }
}

/**
 * The quick switcher palette, the tag picker and the template picker, over the workspace while open, following the
 * tree and the active pane's [editor]; `Mod+T` opens the template picker.
 */
@Composable
private fun Pickers(
    state: VaultTreeState,
    workspaceState: WorkspaceState,
    tree: VaultTreeViewModel,
    editor: EditorViewModel,
    switcher: QuickSwitcherViewModel,
    tagPicker: TagPickerViewModel,
    templatePicker: TemplatePickerViewModel
) {
    FollowQuickSwitcher(switcher = switcher, tree = tree, editor = editor, notes = state.tree.notes)
    FollowTagPicker(tagPicker = tagPicker, editor = editor, notes = state.tree.notes.values)
    FollowTemplatePicker(templatePicker = templatePicker, tree = tree, editor = editor, state = state)
    TemplateShortcut(
        platformConventions = state.platformConventions,
        workspaceState = workspaceState,
        editor = editor,
        onTemplatePicker = templatePicker::onIntent
    )
    val switcherState by switcher.state.collectAsState()
    val tagPickerState by tagPicker.state.collectAsState()
    val templatePickerState by templatePicker.state.collectAsState()
    QuickSwitcherContent(state = switcherState, onIntent = switcher::onIntent)
    TagPickerContent(state = tagPickerState, onIntent = tagPicker::onIntent)
    TemplatePickerContent(
        state = templatePickerState,
        platformConventions = state.platformConventions,
        onIntent = templatePicker::onIntent
    )
}

/** The side panel's requests from the editor's toolbar and outline, as the tabs' intents. */
private fun workspaceIntentOf(
    intent: EditorIntent
): WorkspaceIntent = when (intent) {
    is EditorIntent.OnSidePanelResize -> WorkspaceIntent.OnSidePanelResize(widthDp = intent.widthDp)
    is EditorIntent.OnSidePanelSectionToggle -> WorkspaceIntent.OnSidePanelSectionToggle(section = intent.section)
    is EditorIntent.OnSidePanelModeSelect -> WorkspaceIntent.OnSidePanelModeSelect(mode = intent.mode)
    EditorIntent.OnOutlineFocusRequest -> WorkspaceIntent.OnOutlineFocusRequest
    else -> WorkspaceIntent.OnSidePanelToggle
}

/** The window shortcuts of the workspace, and the view models following each other's effects. */
@Composable
private fun WorkspaceFollowers(
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
private fun FollowQuickSwitcher(
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
private fun FollowTagPicker(
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
 * `Mod+T` (`ROADMAP.md` 1.12): with a note in the active tab, the template picker opens for it, through its editor so
 * the selection is kept; otherwise for a new note, in the folder the active tab shows or holds (the vault root without
 * tabs).
 */
@Composable
private fun TemplateShortcut(
    platformConventions: PlatformConventions,
    workspaceState: WorkspaceState,
    editor: EditorViewModel,
    onTemplatePicker: (TemplatePickerIntent) -> Unit
) {
    val currentState by rememberUpdatedState(newValue = workspaceState)
    val currentEditor by rememberUpdatedState(newValue = editor)
    WindowShortcut(shortcut = platformConventions.templateShortcut) {
        val active = currentState.workspace.activePath
        val kind = active?.let { currentState.kindOf(path = it) }
        if (kind == TabKind.NOTE) {
            currentEditor.onIntent(intent = EditorIntent.OnTemplateRequest)
        } else {
            val folder = if (kind == TabKind.FOLDER) active else active?.parent
            onTemplatePicker(
                TemplatePickerIntent.OnOpen(
                    target = TemplateTarget.NewNote(
                        folder =
                        folder ?: RelativePath.ROOT
                    )
                )
            )
        }
    }
}

/**
 * The template picker lists the vault tree's templates as it changes. A template chosen for the open note goes into it
 * at the cursor; one chosen for a new note makes it in the tree, filled once it is named; closing the picker without
 * choosing gives the keyboard back to the note.
 */
@Composable
private fun FollowTemplatePicker(
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

private const val TEMPLATE_PICKER_KEY_PREFIX = "templates:"
private const val EDITOR_KEY_PREFIX = "editor:"
private const val SWITCHER_KEY_PREFIX = "switcher:"
private const val TAG_PICKER_KEY_PREFIX = "tags:"
private const val WORKSPACE_KEY_PREFIX = "workspace:"

@Preview
@Composable
private fun VaultTreeScreenPreview() {
    NotekeaPreviewThemes {
        VaultTreeContent(
            state = TreePreviewData.state(),
            vaultName = TreePreviewData.VAULT_NAME,
            onIntent = {},
            onSwitchVault = {},
            onSearch = {}
        ) { PreviewNoteContent(path = TreePreviewData.state().selectedPath) }
    }
}
