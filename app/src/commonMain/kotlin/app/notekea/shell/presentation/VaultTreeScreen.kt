package app.notekea.shell.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import app.notekea.ai.presentation.ModelDownloadViewModel
import app.notekea.cards.presentation.StudyViewModel
import app.notekea.chat.presentation.viewmodel.ChatViewModel
import app.notekea.clips.presentation.ClipIntent
import app.notekea.clips.presentation.ClipViewModel
import app.notekea.connections.presentation.NoteMentionsViewModel
import app.notekea.connections.presentation.RelatedNotesViewModel
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.LocalNoteLooks
import app.notekea.core.presentation.designsystem.theme.NoteLooks
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.platform.WindowShortcut
import app.notekea.editor.presentation.model.EditorIntent
import app.notekea.editor.presentation.sidepanel.SidePanelHost
import app.notekea.editor.presentation.view.EditorPaneTools
import app.notekea.editor.presentation.viewmodel.EditorViewModel
import app.notekea.graph.presentation.GraphViewModel
import app.notekea.query.presentation.QueryViewModel
import app.notekea.search.presentation.index.SearchIndexIntent
import app.notekea.search.presentation.index.SearchIndexNotice
import app.notekea.search.presentation.index.SearchIndexViewModel
import app.notekea.search.presentation.switcher.QuickSwitcherContent
import app.notekea.search.presentation.switcher.QuickSwitcherIntent
import app.notekea.search.presentation.switcher.QuickSwitcherViewModel
import app.notekea.shell.domain.Workspace
import app.notekea.sync.presentation.SyncStatusViewModel
import app.notekea.tasks.presentation.TasksViewModel
import app.notekea.templates.presentation.TemplatePickerContent
import app.notekea.templates.presentation.TemplatePickerIntent
import app.notekea.templates.presentation.TemplatePickerViewModel
import app.notekea.templates.presentation.TemplateTarget
import app.notekea.tree.presentation.filepreview.PreviewNoteContent
import app.notekea.tree.presentation.model.TreePreviewData
import app.notekea.tree.presentation.model.VaultTreeState
import app.notekea.tree.presentation.tags.TagPickerContent
import app.notekea.tree.presentation.tags.TagPickerViewModel
import app.notekea.tree.presentation.view.SidebarRailActions
import app.notekea.tree.presentation.view.VaultTreeContent
import app.notekea.tree.presentation.viewmodel.VaultTreeViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private const val NOTICE_MAX_WIDTH_DP = 640

/**
 * Vault workspace entry point for the vault at [vaultPath]: one [VaultTreeViewModel], one [WorkspaceViewModel], one
 * [EditorViewModel] per pane and one [QuickSwitcherViewModel], rendered by [VaultWorkspace] with the palette and the
 * pickers over it. What the tree opens goes to a tab, and the tree selects the active tab's file.
 */
@Composable
fun VaultTreeScreen(
    vaultPath: String,
    vaultName: String,
    onSwitchVault: () -> Unit,
    modifier: Modifier = Modifier,
    onActiveTabChange: (String?) -> Unit = {},
    viewModel: VaultTreeViewModel = vaultViewModel(vaultPath),
    workspaceViewModel: WorkspaceViewModel = vaultViewModel(vaultPath),
    editorViewModels: List<EditorViewModel> =
        List(size = Workspace.MAX_PANES) { vaultViewModel(vaultPath, it) },
    switcherViewModel: QuickSwitcherViewModel = vaultViewModel(vaultPath),
    tagPickerViewModel: TagPickerViewModel = vaultViewModel(vaultPath),
    templatePickerViewModel: TemplatePickerViewModel = vaultViewModel(vaultPath),
    graphViewModel: GraphViewModel = vaultViewModel(vaultPath),
    queryViewModel: QueryViewModel = vaultViewModel(vaultPath),
    tasksViewModel: TasksViewModel = vaultViewModel(vaultPath),
    studyViewModel: StudyViewModel = vaultViewModel(vaultPath),
    searchIndexViewModel: SearchIndexViewModel = vaultViewModel(vaultPath),
    relatedViewModel: RelatedNotesViewModel = vaultViewModel(vaultPath),
    mentionsViewModel: NoteMentionsViewModel = vaultViewModel(vaultPath),
    chatViewModel: ChatViewModel = vaultViewModel(vaultPath),
    modelDownloadViewModel: ModelDownloadViewModel = vaultViewModel(vaultPath),
    syncViewModel: SyncStatusViewModel = vaultViewModel(vaultPath),
    clipViewModel: ClipViewModel = vaultViewModel(vaultPath),
    panes: List<EditorPaneTools> = remember(vaultPath) {
        List(size = Workspace.MAX_PANES) { EditorPaneTools() }
    }
) {
    val state by viewModel.state.collectAsState()
    val workspaceState by workspaceViewModel.state.collectAsState()

    val editors = remember(workspaceViewModel, editorViewModels) {
        PaneEditors(workspaceViewModel, editorViewModels)
    }

    val sidePanel = rememberSidePanel(
        workspaceViewModel,
        relatedViewModel,
        mentionsViewModel,
        chatViewModel,
        editors,
        state
    )

    val viewModels = WorkspaceViewModels(
        graph = graphViewModel,
        query = queryViewModel,
        tasks = tasksViewModel,
        study = studyViewModel,
        tree = viewModel,
        workspace = workspaceViewModel,
        editors = editorViewModels,
        onSwitcher = switcherViewModel::onIntent,
        onTagPicker = tagPickerViewModel::onIntent,
        onTemplatePicker = templatePickerViewModel::onIntent,
        documentFocus = editors.documents,
        onClipRequest = { clipViewModel.onIntent(intent = ClipIntent.OnOpen) }
    )

    FollowVault(
        state,
        workspaceState,
        viewModels,
        sidePanel,
        searchIndexViewModel,
        onActiveTabChange
    )

    FollowAssistants(chatViewModel, clipViewModel, state, viewModels, editors, searchIndexViewModel)

    Box(modifier = modifier) {
        CompositionLocalProvider(LocalNoteLooks provides rememberNoteLooks(state = state)) {
            VaultWorkspace(
                state = state,
                workspaceState = workspaceState,
                vaultName = vaultName,
                viewModels = viewModels,
                panes = panes,
                sidePanel = sidePanel,
                onSwitchVault = onSwitchVault,
                searchIndex = {
                    VaultStatusOf(syncViewModel, searchIndexViewModel, modelDownloadViewModel)
                },
                banner = { SyncConflictsOf(sync = syncViewModel) }
            )
        }

        IndexFollower(
            state,
            viewModels,
            searchIndexViewModel,
            relatedViewModel,
            mentionsViewModel,
            switcherViewModel
        )

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

/** The [T] of the vault at [vaultPath]: every vault has its own, and each editor [pane] of a vault too. */
@Composable
private inline fun <reified T : ViewModel> vaultViewModel(
    vaultPath: String,
    pane: Int = 0
): T = koinViewModel(key = "${T::class.simpleName}:$pane:$vaultPath") { parametersOf(vaultPath) }

/** How the notes of the vault in [state] look: their folders' colors, and which are daily notes or templates. */
@Composable
private fun rememberNoteLooks(state: VaultTreeState): NoteLooks =
    remember(state.tree.notes, state.dailyFolder, state.templatesFolder) {
        NoteLooks.of(
            notePaths = state
                .tree
                .notes
                .keys
                .map { it.value },
            dailyFolder = state.dailyFolder,
            templatesFolder = state.templatesFolder
        )
    }

/** [onChange] hears the name the active tab shows, for the window's title, and null once the vault closes. */
@Composable
private fun FollowActiveTab(
    name: String?,
    onChange: (String?) -> Unit
) {
    val currentOnChange by rememberUpdatedState(newValue = onChange)

    LaunchedEffect(name) { currentOnChange(name) }

    DisposableEffect(Unit) {
        onDispose { currentOnChange(null) }
    }
}

/** The side panel with Connections and the [chat] ([connectionsSidePanel]), on the [editors] and the [tree]. */
@Composable
private fun rememberSidePanel(
    workspace: WorkspaceViewModel,
    related: RelatedNotesViewModel,
    mentions: NoteMentionsViewModel,
    chat: ChatViewModel,
    editors: PaneEditors,
    tree: VaultTreeState
): SidePanelHost {
    val currentNotes by rememberUpdatedState(newValue = tree.tree.notes.keys)

    return remember(workspace, related, mentions, chat, editors) {
        connectionsSidePanel(workspace, related, mentions, chat, editors) { currentNotes }
    }
}

/**
 * The search index follows the vault's notes once the tree's first scan is done, and shows its first large run's
 * notice over the top of the workspace; Connections follow the active editor's note, the
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

    val tabs by viewModels
        .workspace
        .state
        .collectAsState()

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
        // Notes and documents (4.5): both are searched, related and answered from.
        val notes = tree
            .tree
            .readable
            .map { it.file }

        val isLoaded = !tree.isLoading && tree.loadError == null

        viewModel.onIntent(
            intent = SearchIndexIntent.OnNotesChange(
                notes = notes,
                isLoaded = isLoaded
            )
        )
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
    FollowWorkspace(
        state = state,
        viewModels = viewModels,
        sidePanel = sidePanel
    )

    FollowActiveTab(
        name = workspaceState.activeTabName,
        onChange = onActiveTabChange
    )

    FollowTools(
        viewModels = viewModels,
        state = state,
        searchIndex = searchIndex,
        sidePanel = sidePanel
    )
}

/**
 * The quick switcher palette, the tag picker and the template picker, over the workspace while open, following the
 * tree and the active pane's editor; `Mod+T` opens the template picker.
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

    FollowQuickSwitcher(
        switcher = switcher,
        tree = tree,
        editors = editors,
        notes = state.tree.notes
    )

    FollowTagPicker(
        tagPicker = tagPicker,
        editor = editor,
        notes = state.tree.notes.values
    )

    FollowTemplatePicker(
        templatePicker = templatePicker,
        tree = tree,
        editor = editor,
        state = state
    )

    TemplateShortcut(
        platformConventions = state.platformConventions,
        workspaceState = workspaceState,
        editor = editor,
        onTemplatePicker = templatePicker::onIntent
    )

    val switcherState by switcher.state.collectAsState()
    val tagPickerState by tagPicker.state.collectAsState()
    val templatePickerState by templatePicker.state.collectAsState()

    QuickSwitcherContent(
        state = switcherState,
        onIntent = switcher::onIntent
    )

    TagPickerContent(
        state = tagPickerState,
        onIntent = tagPicker::onIntent
    )

    TemplatePickerContent(
        state = templatePickerState,
        platformConventions = state.platformConventions,
        onIntent = templatePicker::onIntent
    )
}

/**
 * `Mod+T`: with a note in the active tab, the template picker opens for it, through its editor so
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
                        folder = folder ?: RelativePath.ROOT
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
