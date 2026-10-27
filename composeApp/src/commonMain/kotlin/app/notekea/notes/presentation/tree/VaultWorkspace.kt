package app.notekea.notes.presentation.tree

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.components.EmptyState
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.notes.domain.AttachmentKind
import app.notekea.notes.domain.FolderCards
import app.notekea.notes.domain.NaturalNameComparator
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.presentation.editor.EditorCallbacks
import app.notekea.notes.presentation.editor.EditorIntent
import app.notekea.notes.presentation.editor.EditorPaneTools
import app.notekea.notes.presentation.editor.EditorScreen
import app.notekea.notes.presentation.editor.EditorState
import app.notekea.notes.presentation.editor.PropertiesPanel
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

/**
 * The vault's sidebar beside the editor's tabs and panes (`ROADMAP.md` 1.11), with the side panel of the active pane's
 * outline or properties on the right (`ROADMAP.md` 1.9, 1.14). Each tab shows a note in its pane's editor, a file's preview, a folder's grid
 * or, for a file that is gone, says so.
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
    val activePane = workspaceState.workspace.activePane
    val activeEditor = viewModels.editors[activePane]
    val activeEditorState by activeEditor.state.collectAsState()
    val isNoteShown = workspaceState.workspace.activePath?.let { workspaceState.kindOf(path = it) } == TabKind.NOTE
    val context = PaneContext(state, workspaceState, viewModels, panes, sidePanel)
    val findNote = { viewModels.onSwitcher(QuickSwitcherIntent.OnOpen(mode = QuickSwitcherMode.NOTES)) }
    VaultTreeContent(
        state = state,
        vaultName = vaultName,
        onIntent = viewModels.tree::onIntent,
        onSwitchVault = onSwitchVault,
        onSearch = findNote
    ) {
        WithSidePanel(
            preferences = workspaceState.sidePanel,
            sidePanel = sidePanel,
            outline = if (isNoteShown) activeEditorState.outline else emptyList(),
            current = panes[activePane].tracker.current,
            platformConventions = state.platformConventions,
            onHeadingClick = { activeEditor.onIntent(EditorIntent.OnHeadingClick(index = it)) },
            onLeave = { activeEditor.onIntent(EditorIntent.OnFocusRequest()) },
            properties = {
                ActiveProperties(
                    workspaceState = workspaceState,
                    sidePanel = sidePanel,
                    onIntent = activeEditor::onIntent,
                    state = activeEditorState.takeIf { isNoteShown }
                )
            }
        ) {
            WorkspaceContent(
                state = workspaceState,
                platformConventions = state.platformConventions,
                onIntent = viewModels.workspace::onIntent,
                onNewTab = findNote
            ) { pane, path, kind -> PaneContent(pane = pane, path = path, kind = kind, context = context) }
        }
    }
}

/**
 * The side panel's Properties tab for the active pane's note ([state], null when its tab shows no note): property edits
 * go to its editor ([onIntent]), and the panel's own requests to the [sidePanel].
 */
@Composable
private fun ActiveProperties(
    workspaceState: WorkspaceState,
    sidePanel: SidePanelHost,
    onIntent: (EditorIntent) -> Unit,
    state: EditorState?
) {
    PropertiesPanel(
        arePropertiesInNote = workspaceState.sidePanel.arePropertiesInNote,
        onIntent = { if (SidePanelHost.isRequest(intent = it)) sidePanel.onIntent(intent = it) else onIntent(it) },
        state = state
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
        onCoverNeeded = { workspace.onIntent(intent = WorkspaceIntent.OnCoverRequest(path = it)) }
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
        arePropertiesInNote = context.workspaceState.sidePanel.arePropertiesInNote,
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
        onDayStep = { tree.onIntent(intent = VaultTreeIntent.OnDayStep(step = it)) }
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
            onSwitchVault = {},
            onSearch = {}
        ) { PreviewNoteContent(path = TreePreviewData.state().selectedPath) }
    }
}
