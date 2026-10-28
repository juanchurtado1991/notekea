package app.notekea.notes.presentation.tree

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.about.AboutDialog
import app.notekea.core.presentation.components.EmptyState
import app.notekea.core.presentation.settings.SettingsDialog
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.notes.domain.AttachmentKind
import app.notekea.notes.domain.FolderCards
import app.notekea.notes.domain.NaturalNameComparator
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.presentation.editor.EditorCallbacks
import app.notekea.notes.presentation.editor.EditorIntent
import app.notekea.notes.presentation.editor.EditorPaneTools
import app.notekea.notes.presentation.editor.EditorScreen
import app.notekea.notes.presentation.editor.SidePanelHost
import app.notekea.notes.presentation.editor.WithSidePanel
import app.notekea.notes.presentation.switcher.QuickSwitcherIntent
import app.notekea.notes.presentation.switcher.QuickSwitcherMode
import app.notekea.notes.presentation.switcher.QuickSwitcherPurpose
import app.notekea.notes.presentation.tags.TagPickerIntent
import app.notekea.notes.presentation.templates.TemplatePickerIntent
import app.notekea.notes.presentation.templates.TemplateTarget
import app.notekea.notes.presentation.workspace.FolderGrid
import app.notekea.notes.presentation.workspace.TabKind
import app.notekea.notes.presentation.workspace.WorkspaceContent
import app.notekea.notes.presentation.workspace.WorkspaceIntent
import app.notekea.notes.presentation.workspace.WorkspaceState
import app.notekea.resources.Res
import app.notekea.resources.tab_missing_message
import app.notekea.resources.tab_missing_title
import org.jetbrains.compose.resources.stringResource
import app.notekea.notes.presentation.editor.SaveStatus
import app.notekea.notes.presentation.editor.EditorViewModel
import app.notekea.resources.tree_untitled_note
import app.notekea.notes.presentation.workspace.WorkspaceStartActions

/**
 * The vault's sidebar beside the editor's tabs and panes (`ROADMAP.md` 1.11), with the side panel of the active pane's
 * outline on the right (`ROADMAP.md` 1.9). Each tab shows a note in its pane's editor, a file's preview, a folder's grid
 * or, for a file that is gone, says so. The status bar runs under all of it (`ROADMAP.md` 1.14).
 */
@Composable
internal fun VaultWorkspace(
    state: VaultTreeState,
    workspaceState: WorkspaceState,
    vaultName: String,
    viewModels: WorkspaceViewModels,
    panes: List<EditorPaneTools>,
    sidePanel: SidePanelHost,
    onSwitchVault: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        WorkspaceBody(
            state = state,
            workspaceState = workspaceState,
            vaultName = vaultName,
            context = PaneContext(state, workspaceState, viewModels, panes, sidePanel),
            onSwitchVault = onSwitchVault,
            modifier = Modifier.weight(weight = 1f)
        )
        ActiveStatusBar(state = state, workspaceState = workspaceState, vaultName = vaultName, viewModels = viewModels)
    }
}

/** The status bar for the active pane's tab, and for its note's editor when it shows one. */
@Composable
private fun ActiveStatusBar(
    state: VaultTreeState,
    workspaceState: WorkspaceState,
    vaultName: String,
    viewModels: WorkspaceViewModels
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
        editor = editorState.takeIf { kind == TabKind.NOTE },
        buffer = activeEditor.buffer
    )
}

/** The rail, the sidebar, the tabs and panes and the side panel, with About or Settings over them while open. */
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
    val isNoteShown = workspaceState.workspace.activePath?.let { workspaceState.kindOf(path = it) } == TabKind.NOTE
    val findNote = { viewModels.onSwitcher(QuickSwitcherIntent.OnOpen(mode = QuickSwitcherMode.NOTES)) }
    var dialog by remember { mutableStateOf<WorkspaceDialog?>(value = null) }
    VaultTreeContent(
        state = state,
        vaultName = vaultName,
        onIntent = viewModels.tree::onIntent,
        rail = railActionsOf(sidePanel = sidePanel, onSearch = findNote, onSwitchVault = onSwitchVault) { dialog = it },
        modifier = modifier,
        isSidePanelVisible = if (sidePanel.isOverlay) sidePanel.isOverlayOpen else workspaceState.sidePanel.isVisible
    ) {
        WithSidePanel(
            preferences = workspaceState.sidePanel,
            sidePanel = sidePanel,
            outline = if (isNoteShown) activeEditorState.outline else emptyList(),
            current = panes[activePane].tracker.current,
            platformConventions = state.platformConventions,
            onHeadingClick = { activeEditor.onIntent(EditorIntent.OnHeadingClick(index = it)) },
            onLeave = { activeEditor.onIntent(EditorIntent.OnFocusRequest()) },
            info = activeEditorState.info.takeIf { isNoteShown }
        ) {
            WorkspaceContent(
                state = workspaceState,
                platformConventions = state.platformConventions,
                onIntent = viewModels.workspace::onIntent,
                startActions = startActionsOf(viewModels = viewModels, onFindNote = findNote),
                unsavedPaths = unsavedPathsOf(editors = viewModels.editors)
            ) { pane, path, kind -> PaneContent(pane = pane, path = path, kind = kind, context = context) }
        }
    }
    WorkspaceDialogs(platformConventions = state.platformConventions, onDismiss = { dialog = null }, dialog = dialog)
}

/** The notes the panes' editors hold with changes not saved yet, for their tabs' dots. */
@Composable
private fun unsavedPathsOf(
    editors: List<EditorViewModel>
): Set<RelativePath> {
    val paths = editors.mapNotNull { editor ->
        val editorState by editor.state.collectAsState()
        editorState.notePath.takeIf { editorState.saveStatus == SaveStatus.UNSAVED }
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
        onNewNote = { viewModels.tree.onIntent(intent = VaultTreeIntent.OnNewNoteClick(baseName = untitledNote)) },
        onToday = { viewModels.tree.onIntent(intent = VaultTreeIntent.OnTodayClick) },
        onSearchNotes = { viewModels.onSwitcher(QuickSwitcherIntent.OnOpen(mode = QuickSwitcherMode.CONTENT)) }
    )
}

/** The rail's actions: search, the side panel, another vault, and About or Settings opened with [onDialog]. */
private fun railActionsOf(
    sidePanel: SidePanelHost,
    onSearch: () -> Unit,
    onSwitchVault: () -> Unit,
    onDialog: (WorkspaceDialog) -> Unit
) = SidebarRailActions(
    onSearch = onSearch,
    onSidePanelToggle = { sidePanel.onIntent(intent = EditorIntent.OnSidePanelToggle) },
    onSwitchVault = onSwitchVault,
    onAbout = { onDialog(WorkspaceDialog.ABOUT) },
    onSettings = { onDialog(WorkspaceDialog.SETTINGS) }
)

/** About or Settings over the workspace, while one is open. */
@Composable
private fun WorkspaceDialogs(
    platformConventions: PlatformConventions,
    onDismiss: () -> Unit,
    dialog: WorkspaceDialog?
) {
    when (dialog) {
        WorkspaceDialog.ABOUT -> AboutDialog(platformConventions = platformConventions, onDismiss = onDismiss)
        WorkspaceDialog.SETTINGS -> SettingsDialog(onDismiss = onDismiss)
        null -> Unit
    }
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
        TabKind.NOTE -> NoteEditor(pane = pane, path = path, context = context)

        TabKind.FILE -> state.tree.attachments[path]?.let { file ->
            FileDetails(
                preview = context.workspaceState.previews[path]?.takeIf { it.file == file }
                    ?: FilePreview(file = file, kind = AttachmentKind.of(name = path.name)),
                platformConventions = state.platformConventions,
                onIntent = context.viewModels.tree::onIntent
            )
        }

        TabKind.FOLDER -> FolderTab(folder = path, context = context)

        TabKind.MISSING -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            EmptyState(
                title = stringResource(resource = Res.string.tab_missing_title),
                message = stringResource(resource = Res.string.tab_missing_message)
            )
        }
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
        fileCount = (tree.notes.keys + tree.attachments.keys).count { it.isWithin(folder = folder) },
        cards = remember(tree, folder) { folderCards.of(folder = folder, tree = tree) },
        covers = context.workspaceState.covers,
        onOpen = { workspace.onIntent(intent = WorkspaceIntent.OnOpen(path = it)) },
        onCoverNeeded = { path, size ->
            workspace.onIntent(intent = WorkspaceIntent.OnCoverRequest(path = path, size = size))
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
        callbacks = editorCallbacksOf(viewModels = context.viewModels, path = path),
        pane = context.panes[pane],
        sidePanel = context.sidePanel,
        isActive = context.workspaceState.workspace.activePane == pane,
        dayNeighbors = state.dayNeighbors.takeIf { state.selectedPath == path },
        isBookmarked = path in state.bookmarks
    )
}

/** What the editor of the note at [path] asks of the tree, the tabs and the pickers. */
private fun editorCallbacksOf(
    viewModels: WorkspaceViewModels,
    path: RelativePath
): EditorCallbacks {
    val tree = viewModels.tree
    val workspace = viewModels.workspace
    return EditorCallbacks(
        onRenamed = {
            tree.onIntent(intent = VaultTreeIntent.OnEntryRenamed(from = it.from, to = it.to, links = it.links))
            workspace.onIntent(intent = WorkspaceIntent.OnRenamed(from = it.from, to = it.to))
        },
        onClosed = { workspace.onIntent(intent = WorkspaceIntent.OnDeleted(path = path)) },
        onOpenLink = {
            tree.onIntent(
                intent = VaultTreeIntent.OnFollowLink(
                    source = it.source,
                    target = it.target,
                    kind = it.kind,
                    inBackground = it.inBackground
                )
            )
        },
        onSearchTag = { tag ->
            viewModels.onSwitcher(QuickSwitcherIntent.OnOpen(mode = QuickSwitcherMode.CONTENT, query = TAG_MARK + tag))
        },
        onRequestNoteLink = {
            viewModels.onSwitcher(
                QuickSwitcherIntent.OnOpen(mode = QuickSwitcherMode.NOTES, purpose = QuickSwitcherPurpose.LINK)
            )
        },
        onRequestTag = { query -> viewModels.onTagPicker(TagPickerIntent.OnOpen(query = query)) },
        onBookmarkToggle = { tree.onIntent(intent = BookmarkIntent.OnToggle(path = it)) },
        onRequestTemplate = {
            viewModels.onTemplatePicker(TemplatePickerIntent.OnOpen(target = TemplateTarget.CurrentNote))
        },
        onDayStep = { tree.onIntent(intent = VaultTreeIntent.OnDayStep(step = it)) },
        onRevealFolder = { tree.onIntent(intent = VaultTreeIntent.OnRevealInTree(path = it)) }
    )
}

private const val TAG_MARK = "#"

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
