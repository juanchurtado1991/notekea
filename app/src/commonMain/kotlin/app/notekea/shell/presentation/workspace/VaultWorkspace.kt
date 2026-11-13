package app.notekea.shell.presentation.workspace

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.cards.presentation.StudyContent
import app.notekea.core.documents.domain.DocumentFormat
import app.notekea.core.notes.domain.AttachmentKind
import app.notekea.core.notes.domain.NaturalNameComparator
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.designsystem.components.feedback.EmptyState
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.platform.LocalWindowDialogs
import app.notekea.editor.presentation.EditorScreen
import app.notekea.editor.presentation.model.EditorIntent
import app.notekea.editor.presentation.model.SaveStatus
import app.notekea.editor.presentation.sidepanel.SidePanelHost
import app.notekea.editor.presentation.sidepanel.WithSidePanel
import app.notekea.editor.presentation.view.EditorPaneTools
import app.notekea.editor.presentation.viewmodel.EditorViewModel
import app.notekea.graph.presentation.GraphContent
import app.notekea.graph.presentation.view.GraphInspector
import app.notekea.query.presentation.QueryContent
import app.notekea.resources.Res
import app.notekea.resources.tab_missing_message
import app.notekea.resources.tab_missing_title
import app.notekea.resources.tree_untitled_note
import app.notekea.search.presentation.switcher.QuickSwitcherIntent
import app.notekea.search.presentation.switcher.QuickSwitcherMode
import app.notekea.shell.presentation.tabs.FileTab
import app.notekea.shell.presentation.tabs.FolderGrid
import app.notekea.shell.presentation.tabs.FollowQueryBlocks
import app.notekea.shell.presentation.tabs.TabKind
import app.notekea.shell.presentation.tabs.WithYouTubeTab
import app.notekea.shell.presentation.tabs.YouTubeTab
import app.notekea.tasks.presentation.TasksContent
import app.notekea.tree.domain.FolderCards
import app.notekea.tree.presentation.filepreview.FileDetails
import app.notekea.tree.presentation.filepreview.FilePreview
import app.notekea.tree.presentation.filepreview.PreviewNoteContent
import app.notekea.tree.presentation.model.TreePreviewData
import app.notekea.tree.presentation.model.VaultTreeIntent
import app.notekea.tree.presentation.model.VaultTreeState
import app.notekea.tree.presentation.view.EntryMenuArea
import app.notekea.tree.presentation.view.SidebarRailActions
import app.notekea.tree.presentation.view.VaultTreeContent
import app.notekea.tree.presentation.view.asTreeRow
import org.jetbrains.compose.resources.stringResource

/**
 * The vault's sidebar beside the editor's tabs and panes, with the side panel of the active pane on the right. Each tab
 * shows a note in its pane's editor, a file's preview, a folder's grid or, for a file that is gone, says so. The status
 * bar runs under all of it, with the search index's status ([searchIndex]); a [banner] (a sync conflict) over it.
 */
@Composable
internal fun VaultWorkspace(
    state: VaultTreeState,
    workspaceState: WorkspaceState,
    vaultName: String,
    viewModels: WorkspaceViewModels,
    panes: List<EditorPaneTools>,
    sidePanel: SidePanelHost,
    onSwitchVault: () -> Unit,
    searchIndex: @Composable () -> Unit = {},
    banner: @Composable () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        banner()

        WorkspaceBody(
            state = state,
            workspaceState = workspaceState,
            vaultName = vaultName,
            context = PaneContext(state, workspaceState, viewModels, panes, sidePanel),
            onSwitchVault = onSwitchVault,
            modifier = Modifier.weight(weight = 1f)
        )

        ActiveStatusBar(state, workspaceState, vaultName, viewModels, searchIndex)
    }
}

/** The status bar for the active pane's tab, and for its note's editor when it shows one. */
@Composable
private fun ActiveStatusBar(
    state: VaultTreeState,
    workspaceState: WorkspaceState,
    vaultName: String,
    viewModels: WorkspaceViewModels,
    searchIndex: @Composable () -> Unit
) {
    val activeEditor = viewModels.editors[workspaceState.workspace.activePane]
    val editorState by activeEditor.state.collectAsState()
    val path = workspaceState.workspace.activePath

    val kind = path?.let { workspaceState.kindOf(path = it) }

    WorkspaceStatusBar(
        vaultName = vaultName,
        isLoading = state.isLoading,
        onReveal = { viewModels.tree.onIntent(intent = VaultTreeIntent.OnRevealInTree(path = it)) },
        path = path,
        kind = kind,
        editor = editorState.takeIf {
            kind == TabKind.NOTE
        },
        buffer = activeEditor.buffer,
        searchIndex = searchIndex
    )
}

/** The rail, the sidebar, the tabs and panes and the side panel; the rail's About and Settings open over the window. */
@Composable
private fun WorkspaceBody(
    state: VaultTreeState,
    workspaceState: WorkspaceState,
    vaultName: String,
    context: PaneContext,
    onSwitchVault: () -> Unit,
    modifier: Modifier
) {
    val viewModels = context.viewModels
    val panes = context.panes
    val sidePanel = context.sidePanel
    val activePane = workspaceState.workspace.activePane
    val activeEditor = viewModels.editors[activePane]
    val activeEditorState by activeEditor.state.collectAsState()

    val activeKind = workspaceState
        .workspace
        .activePath
        ?.let { workspaceState.kindOf(path = it) }

    val isNoteShown = activeKind == TabKind.NOTE

    val findNote = {
        viewModels.onSwitcher(QuickSwitcherIntent.OnOpen(mode = QuickSwitcherMode.NOTES))
    }

    val dialogs = LocalWindowDialogs.current

    val askSwitchVault = switchVaultAsked(
        vaultName = vaultName,
        conventions = state.platformConventions,
        onSwitchVault = onSwitchVault
    )

    val isPanelShown =
        if (sidePanel.isOverlay) sidePanel.isOverlayOpen else workspaceState.sidePanel.isVisible

    VaultTreeContent(
        state = state,
        vaultName = vaultName,
        onIntent = viewModels.tree::onIntent,
        rail = railActionsOf(
            viewModels = viewModels,
            sidePanel = sidePanel,
            onSearch = findNote,
            onSwitchVault = askSwitchVault,
            onDialog = { dialogs.shown = it }
        ),
        modifier = modifier,
        isSidePanelVisible = isPanelShown
    ) {
        val outline = if (isNoteShown) activeEditorState.outline else emptyList()

        WithSidePanel(
            preferences = workspaceState.sidePanel,
            sidePanel = sidePanel,
            outline = outline,
            current = { panes[activePane].tracker.current },
            platformConventions = state.platformConventions,
            onHeadingClick = { activeEditor.onIntent(EditorIntent.OnHeadingClick(index = it)) },
            onLeave = { activeEditor.onIntent(EditorIntent.OnFocusRequest()) },
            info = activeEditorState.info.takeIf { isNoteShown },
            inspector = graphInspectorOf(viewModels = viewModels).takeIf {
                activeKind == TabKind.GRAPH
            }
        ) {
            FollowQueryBlocks(query = viewModels.query) {
                WithYouTubeTab(
                    onPlay = { id, title ->
                        viewModels.workspace.onIntent(
                            intent = WorkspaceIntent.OnYouTubePlay(
                                id = id,
                                title = title
                            )
                        )
                    }
                ) {
                    PaneArea(
                        state = state,
                        context = context,
                        onFindNote = findNote
                    )
                }
            }
        }
    }
}

/** The tabs and the panes. */
@Composable
private fun PaneArea(
    state: VaultTreeState,
    context: PaneContext,
    onFindNote: () -> Unit
) {
    val viewModels = context.viewModels

    WorkspaceContent(
        state = context.workspaceState,
        platformConventions = state.platformConventions,
        onIntent = viewModels.workspace::onIntent,
        startActions = startActionsOf(
            viewModels = viewModels,
            onFindNote = onFindNote
        ),
        unsavedPaths = unsavedPathsOf(editors = viewModels.editors)
    ) { pane, path, kind ->
        PaneContent(
            pane = pane,
            path = path,
            kind = kind,
            context = context
        )
    }
}

/** What the side panel's inspector shows while the graph's tab is the active one. */
private fun graphInspectorOf(viewModels: WorkspaceViewModels): @Composable () -> Unit = {
    GraphInspector(
        state = viewModels
            .graph
            .state
            .collectAsState()
            .value,
        onIntent = viewModels.graph::onIntent
    )
}

/** The notes the panes' editors hold with changes not saved yet, for their tabs' dots. */
@Composable
private fun unsavedPathsOf(editors: List<EditorViewModel>): Set<RelativePath> {
    val paths = editors.mapNotNull { editor ->
        val editorState by editor.state.collectAsState()

        editorState.notePath.takeIf {
            editorState.saveStatus == SaveStatus.UNSAVED
        }
    }.toSet()

    // The same set while its paths stay the same, so the tabs skip recomposing on every keystroke.
    return remember(paths) { paths }
}

/** What an editor without tabs offers: find a note, a new note, today's note and a search in the notes. */
@Composable
private fun startActionsOf(
    viewModels: WorkspaceViewModels,
    onFindNote: () -> Unit
): WorkspaceStartActions {
    val untitledNote = stringResource(resource = Res.string.tree_untitled_note)

    return WorkspaceStartActions(
        onFindNote = onFindNote,
        onNewNote = {
            viewModels.tree.onIntent(
                intent = VaultTreeIntent.OnNewNoteClick(baseName = untitledNote)
            )
        },
        onToday = { viewModels.tree.onIntent(intent = VaultTreeIntent.OnTodayClick) },
        onSearchNotes = {
            viewModels.onSwitcher(QuickSwitcherIntent.OnOpen(mode = QuickSwitcherMode.CONTENT))
        },
        onClipPage = viewModels.onClipRequest
    )
}

/** What a pane's active tab shows, by what [path] is. */
@Composable
private fun PaneContent(
    pane: Int,
    path: RelativePath,
    kind: TabKind,
    context: PaneContext
) {
    val state = context.state

    when (kind) {
        TabKind.NOTE -> NoteEditor(
            pane = pane,
            path = path,
            context = context
        )

        TabKind.FILE -> FileTab(
            path = path,
            context = context
        )

        TabKind.FOLDER -> FolderTab(
            folder = path,
            context = context
        )

        TabKind.GRAPH -> GraphContent(
            state = context
                .viewModels
                .graph
                .state
                .collectAsState()
                .value,
            platformConventions = state.platformConventions,
            onIntent = context.viewModels.graph::onIntent,
            isActive = context.workspaceState.workspace.activePane == pane
        )

        TabKind.QUERY -> QueryContent(
            state = context
                .viewModels
                .query
                .state
                .collectAsState()
                .value,
            onIntent = context.viewModels.query::onIntent,
            saveTarget = context.workspaceState.noteAtHand
        )

        TabKind.TASKS -> TasksContent(
            state = context
                .viewModels
                .tasks
                .state
                .collectAsState()
                .value,
            onIntent = context.viewModels.tasks::onIntent
        )

        TabKind.STUDY -> StudyContent(
            state = context
                .viewModels
                .study
                .state
                .collectAsState()
                .value,
            onIntent = context.viewModels.study::onIntent
        )

        TabKind.YOUTUBE -> YouTubeTab(video = context.workspaceState.video)

        TabKind.MISSING -> MissingTab()
    }
}

@Composable
private fun MissingTab() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        EmptyState(
            title = stringResource(resource = Res.string.tab_missing_title),
            message = stringResource(resource = Res.string.tab_missing_message)
        )
    }
}

/** A folder's grid: a card opens its folder, note or file in a tab, and asks for its picture as it comes into view. */
@Composable
private fun FolderTab(
    folder: RelativePath,
    context: PaneContext
) {
    val tree = context.state.tree
    val workspace = context.viewModels.workspace

    val folderCards = remember { FolderCards(naturalNameComparator = NaturalNameComparator()) }

    FolderGrid(
        folder = folder,
        fileCount = remember(tree, folder) { tree.fileCountIn(folder = folder) },
        cards = remember(tree, folder) {
            folderCards.of(
                folder = folder,
                tree = tree
            )
        },
        covers = context.workspaceState.covers,
        onOpen = { workspace.onIntent(intent = WorkspaceIntent.OnOpen(path = it)) },
        onCoverNeeded = { path, size ->
            workspace.onIntent(
                intent = WorkspaceIntent.OnCoverRequest(
                    path = path,
                    size = size
                )
            )
        },
        cardMenu = { card, content ->
            EntryMenuArea(card.asTreeRow(), {
                context.state
            }, context.viewModels.tree::onIntent, content = content)
        }
    )
}

/**
 * The note of a pane's tab in that pane's editor, following the tree: a rename or a closed note is told to the tree
 * and the tabs, a link opens its note in a tab, a clicked `#tag` is searched, and the toolbar's note link and tag open
 * their pickers. Its shortcuts work while its pane is the active one.
 */
@Composable
private fun NoteEditor(
    pane: Int,
    path: RelativePath,
    context: PaneContext
) {
    val state = context.state

    EditorScreen(
        notePath = path,
        viewModel = context.viewModels.editors[pane],
        platformConventions = state.platformConventions,
        callbacks = editorCallbacksOf(
            viewModels = context.viewModels,
            pane = pane,
            path = path
        ),
        pane = context.panes[pane],
        sidePanel = context.sidePanel,
        isActive = context.workspaceState.workspace.activePane == pane,
        dayNeighbors = state.dayNeighbors.takeIf {
            state.selectedPath == path
        },
        isBookmarked = path in state.bookmarks
    )
}

@Preview
@Composable
private fun VaultWorkspacePreview() {
    NotekeaPreviewThemes {
        VaultTreeContent(
            state = TreePreviewData.state(),
            vaultName = TreePreviewData.VAULT_NAME,
            onIntent = {},
            rail = SidebarRailActions()
        ) { PreviewNoteContent(path = TreePreviewData.state().selectedPath) }
    }
}
