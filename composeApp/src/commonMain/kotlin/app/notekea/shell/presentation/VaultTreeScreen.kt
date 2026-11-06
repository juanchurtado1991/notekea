package app.notekea.shell.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import app.notekea.core.presentation.theme.NotekeaDimensions
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.runtime.DisposableEffect
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
import app.notekea.core.notes.domain.RelativePath
import app.notekea.shell.domain.Workspace
import app.notekea.editor.presentation.EditorIntent
import app.notekea.editor.presentation.EditorPaneTools
import app.notekea.editor.presentation.EditorViewModel
import app.notekea.editor.presentation.SidePanelHost
import app.notekea.search.presentation.switcher.QuickSwitcherContent
import app.notekea.graph.presentation.GraphViewModel
import app.notekea.query.presentation.QueryViewModel
import app.notekea.tasks.presentation.TasksViewModel
import app.notekea.search.presentation.switcher.QuickSwitcherIntent
import app.notekea.search.presentation.switcher.QuickSwitcherViewModel
import app.notekea.tree.presentation.tags.TagPickerContent
import app.notekea.tree.presentation.tags.TagPickerViewModel
import app.notekea.templates.presentation.TemplatePickerContent
import app.notekea.templates.presentation.TemplatePickerIntent
import app.notekea.templates.presentation.TemplatePickerViewModel
import app.notekea.templates.presentation.TemplateTarget
import app.notekea.shell.presentation.TabKind
import app.notekea.shell.presentation.WorkspaceIntent
import app.notekea.shell.presentation.WorkspaceState
import app.notekea.shell.presentation.WorkspaceViewModel
import app.notekea.search.presentation.index.SearchIndexIntent
import app.notekea.connections.presentation.NoteMentionsViewModel
import app.notekea.connections.presentation.RelatedNotesViewModel
import app.notekea.search.presentation.index.SearchIndexNotice
import app.notekea.search.presentation.index.SearchIndexStatus
import app.notekea.search.presentation.index.SearchIndexViewModel
import app.notekea.core.notes.domain.LinkResolver
import app.notekea.editor.domain.FormatAction
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import app.notekea.tree.presentation.PreviewNoteContent
import app.notekea.tree.presentation.SidebarRailActions
import app.notekea.tree.presentation.TreePreviewData
import app.notekea.tree.presentation.VaultTreeContent
import app.notekea.tree.presentation.VaultTreeState
import app.notekea.tree.presentation.VaultTreeViewModel

private const val TEMPLATE_PICKER_KEY_PREFIX = "templates:"
private const val EDITOR_KEY_PREFIX = "editor:"
private const val GRAPH_KEY_PREFIX = "graph:"
private const val QUERY_KEY_PREFIX = "query:"
private const val TASKS_KEY_PREFIX = "tasks:"
private const val SWITCHER_KEY_PREFIX = "switcher:"
private const val SEARCH_INDEX_KEY_PREFIX = "search-index:"
private const val NOTICE_MAX_WIDTH_DP = 640
private const val MENTIONS_KEY_PREFIX = "mentions:"
private const val RELATED_KEY_PREFIX = "related:"
private const val TAG_PICKER_KEY_PREFIX = "tags:"
private const val WORKSPACE_KEY_PREFIX = "workspace:"

/**
 * Vault workspace entry point for the vault at [vaultPath]: one [VaultTreeViewModel], one [WorkspaceViewModel] for the
 * tabs and panes, one [EditorViewModel] per pane and one [QuickSwitcherViewModel] per vault (opening another vault
 * starts fresh), rendered by [VaultWorkspace] with the quick switcher palette, the tag picker and the template picker
 * over it. They follow each other: what the tree opens (a click, a link, the quick switcher, Today) opens in a tab, and
 * the tree selects the active tab's file; a rename moves the tabs and the editors' notes, a deletion closes its tabs;
 * the palette and the pickers work on the note of the active pane. The workspace shortcuts work whatever has focus.
 * [onActiveTabChange] hears the name the active tab shows.
 */
@Composable
fun VaultTreeScreen(
    vaultPath: String,
    vaultName: String,
    onSwitchVault: () -> Unit,
    modifier: Modifier = Modifier,
    onActiveTabChange: (String?) -> Unit = {},
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
    ) { parametersOf(vaultPath) },
    graphViewModel: GraphViewModel = koinViewModel(key = GRAPH_KEY_PREFIX + vaultPath) { parametersOf(vaultPath) },
    queryViewModel: QueryViewModel = koinViewModel(key = QUERY_KEY_PREFIX + vaultPath) { parametersOf(vaultPath) },
    tasksViewModel: TasksViewModel = koinViewModel(key = TASKS_KEY_PREFIX + vaultPath) { parametersOf(vaultPath) },
    searchIndexViewModel: SearchIndexViewModel = koinViewModel(
        key = SEARCH_INDEX_KEY_PREFIX + vaultPath
    ) { parametersOf(vaultPath) },
    relatedViewModel: RelatedNotesViewModel = koinViewModel(
        key = RELATED_KEY_PREFIX + vaultPath
    ) { parametersOf(vaultPath) },
    mentionsViewModel: NoteMentionsViewModel = koinViewModel(
        key = MENTIONS_KEY_PREFIX + vaultPath
    ) { parametersOf(vaultPath) },
    panes: List<EditorPaneTools> = remember(vaultPath) { List(size = Workspace.MAX_PANES) { EditorPaneTools() } }
) {
    val state by viewModel.state.collectAsState()
    val workspaceState by workspaceViewModel.state.collectAsState()
    val editors = remember(workspaceViewModel, editorViewModels) { PaneEditors(workspaceViewModel, editorViewModels) }
    val sidePanel = rememberSidePanel(workspaceViewModel, relatedViewModel, mentionsViewModel, editors, state)
    val viewModels = WorkspaceViewModels(
        graph = graphViewModel,
        query = queryViewModel,
        tasks = tasksViewModel,
        tree = viewModel,
        workspace = workspaceViewModel,
        editors = editorViewModels,
        onSwitcher = switcherViewModel::onIntent,
        onTagPicker = tagPickerViewModel::onIntent,
        onTemplatePicker = templatePickerViewModel::onIntent
    )
    FollowVault(state, workspaceState, viewModels, sidePanel, searchIndexViewModel, onActiveTabChange)
    Box(modifier = modifier) {
        VaultWorkspace(
            state = state,
            workspaceState = workspaceState,
            vaultName = vaultName,
            viewModels = viewModels,
            panes = panes,
            sidePanel = sidePanel,
            onSwitchVault = onSwitchVault,
            searchIndex = { SearchIndexStatusOf(viewModel = searchIndexViewModel) }
        )
        IndexFollower(state, viewModels, searchIndexViewModel, relatedViewModel, mentionsViewModel, switcherViewModel)
        Pickers(
            state = state,
            workspaceState = workspaceState,
            tree = viewModel,
            editors = editors,
            switcher = switcherViewModel,
            tagPicker = tagPickerViewModel,
            templatePicker = templatePickerViewModel
        )
    }
}

/** [onChange] hears the name the active tab shows, for the window's title, and null once the vault closes. */
@Composable
private fun FollowActiveTab(
    name: String?,
    onChange: (String?) -> Unit
) {
    val currentOnChange by rememberUpdatedState(newValue = onChange)
    LaunchedEffect(name) { currentOnChange(name) }
    DisposableEffect(Unit) { onDispose { currentOnChange(null) } }
}

/** The side panel with the inspector's Connections ([connectionsSidePanel]), on the [editors] and the [tree]. */
@Composable
private fun rememberSidePanel(
    workspace: WorkspaceViewModel,
    related: RelatedNotesViewModel,
    mentions: NoteMentionsViewModel,
    editors: PaneEditors,
    tree: VaultTreeState
): SidePanelHost {
    val currentNotes by rememberUpdatedState(newValue = tree.tree.notes.keys)
    return remember(workspace, related, mentions, editors) {
        connectionsSidePanel(workspace, related, mentions, editors) { currentNotes }
    }
}

/** The search index's status in the status bar (`ROADMAP.md` 2.3). */
@Composable
private fun SearchIndexStatusOf(
    viewModel: SearchIndexViewModel
) {
    val state by viewModel.state.collectAsState()
    SearchIndexStatus(state = state, onIntent = viewModel::onIntent)
}

/**
 * The search index follows the vault's notes once the tree's first scan is done, and shows its first large run's
 * notice over the top of the workspace (`ROADMAP.md` 2.3); Connections follow the active [editor]'s note, the
 * index's runs and the vault (2.4, 2.5).
 */
@Composable
private fun BoxScope.IndexFollower(
    tree: VaultTreeState,
    viewModels: WorkspaceViewModels,
    viewModel: SearchIndexViewModel,
    related: RelatedNotesViewModel,
    mentions: NoteMentionsViewModel,
    switcher: QuickSwitcherViewModel
) {
    val state by viewModel.state.collectAsState()
    val tabs by viewModels.workspace.state.collectAsState()
    val isIndexReady = !state.isRunning && state.readyNotes != null
    LaunchedEffect(switcher, isIndexReady) {
        switcher.onIntent(intent = QuickSwitcherIntent.OnIndexChange(isReady = isIndexReady))
    }
    FollowConnections(
        tree = tree,
        editor = viewModels.editors[tabs.workspace.activePane],
        isIndexReady = isIndexReady,
        related = related,
        mentions = mentions
    )
    LaunchedEffect(tree.tree, tree.isLoading) {
        val notes = tree.tree.notes.values.map { it.file }
        val isLoaded = !tree.isLoading && tree.loadError == null
        viewModel.onIntent(intent = SearchIndexIntent.OnNotesChange(notes = notes, isLoaded = isLoaded))
    }
    if (!state.showsNotice) return
    SearchIndexNotice(
        onIntent = viewModel::onIntent,
        modifier = Modifier
            .align(alignment = Alignment.TopCenter)
            .padding(all = NotekeaDimensions.SpacingL)
            .widthIn(max = NOTICE_MAX_WIDTH_DP.dp)
    )
}

/** What goes on around the workspace while the vault is open, whatever its tabs show. */
@Composable
private fun FollowVault(
    state: VaultTreeState,
    workspaceState: WorkspaceState,
    viewModels: WorkspaceViewModels,
    sidePanel: SidePanelHost,
    searchIndex: SearchIndexViewModel,
    onActiveTabChange: (String?) -> Unit
) {
    FollowWorkspace(state = state, viewModels = viewModels, sidePanel = sidePanel)
    FollowActiveTab(name = workspaceState.activeTabName, onChange = onActiveTabChange)
    FollowTools(viewModels = viewModels, state = state, searchIndex = searchIndex, sidePanel = sidePanel)
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
    editors: PaneEditors,
    switcher: QuickSwitcherViewModel,
    tagPicker: TagPickerViewModel,
    templatePicker: TemplatePickerViewModel
) {
    val editor = editors.active
    FollowQuickSwitcher(switcher = switcher, tree = tree, editors = editors, notes = state.tree.notes)
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
