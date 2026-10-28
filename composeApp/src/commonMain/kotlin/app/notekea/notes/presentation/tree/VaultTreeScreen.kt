package app.notekea.notes.presentation.tree

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.platform.WindowShortcut
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.Workspace
import app.notekea.notes.presentation.editor.EditorIntent
import app.notekea.notes.presentation.editor.EditorPaneTools
import app.notekea.notes.presentation.editor.EditorViewModel
import app.notekea.notes.presentation.editor.SidePanelHost
import app.notekea.notes.presentation.switcher.QuickSwitcherContent
import app.notekea.notes.presentation.switcher.QuickSwitcherViewModel
import app.notekea.notes.presentation.tags.TagPickerContent
import app.notekea.notes.presentation.tags.TagPickerViewModel
import app.notekea.notes.presentation.templates.TemplatePickerContent
import app.notekea.notes.presentation.templates.TemplatePickerIntent
import app.notekea.notes.presentation.templates.TemplatePickerViewModel
import app.notekea.notes.presentation.templates.TemplateTarget
import app.notekea.notes.presentation.workspace.TabKind
import app.notekea.notes.presentation.workspace.WorkspaceIntent
import app.notekea.notes.presentation.workspace.WorkspaceState
import app.notekea.notes.presentation.workspace.WorkspaceViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private const val TEMPLATE_PICKER_KEY_PREFIX = "templates:"
private const val EDITOR_KEY_PREFIX = "editor:"
private const val SWITCHER_KEY_PREFIX = "switcher:"
private const val TAG_PICKER_KEY_PREFIX = "tags:"
private const val WORKSPACE_KEY_PREFIX = "workspace:"

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
    FollowWorkspace(state = state, viewModels = viewModels, sidePanel = sidePanel)
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

@Preview
@Composable
private fun VaultTreeScreenPreview() {
    NotekeaPreviewThemes {
        VaultTreeContent(
            state = TreePreviewData.state(),
            vaultName = TreePreviewData.VAULT_NAME,
            onIntent = {},
            rail = SidebarRailActions()
        ) { PreviewNoteContent(path = TreePreviewData.state().selectedPath) }
    }
}
