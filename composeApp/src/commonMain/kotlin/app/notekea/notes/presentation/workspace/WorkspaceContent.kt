package app.notekea.notes.presentation.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.platform.domain.Shortcut
import app.notekea.core.presentation.components.EmptyState
import app.notekea.core.presentation.components.NotekeaActionRow
import app.notekea.core.presentation.platform.keys
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.preview.PreviewPlatformConventions
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.notes.domain.EditorPane
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.VaultTree
import app.notekea.notes.domain.Workspace
import app.notekea.resources.Res
import app.notekea.resources.workspace_empty_message
import app.notekea.resources.workspace_empty_title
import app.notekea.resources.workspace_start_find
import app.notekea.resources.workspace_start_new
import app.notekea.resources.workspace_start_search
import app.notekea.resources.workspace_start_today
import org.jetbrains.compose.resources.stringResource

private const val PREVIEW_HEIGHT_DP = 320
private const val PREVIEW_WIDTH_DP = 900
private const val SPLIT_DROP_ZONE = 0.75f
private const val START_WIDTH_DP = 440

/**
 * The editor's panes side by side (`ROADMAP.md` 1.11), each with its tabs above what its active tab shows
 * ([paneContent]: a note's editor, a file's preview, a folder's grid). A click in a pane makes it the one the keyboard
 * works in. A tab dropped among its pane's tabs takes that place; dropped on the other pane, it moves there; dropped on
 * the right quarter of a single pane, it splits it and moves to the new pane. The "+" after the tabs finds a note to
 * open in a new tab; a tab of [unsavedPaths] shows a dot until it is saved. Without any tab, the pane offers [startActions] with their shortcuts.
 */
@Composable
fun WorkspaceContent(
    state: WorkspaceState,
    platformConventions: PlatformConventions,
    onIntent: (WorkspaceIntent) -> Unit,
    startActions: WorkspaceStartActions,
    unsavedPaths: Set<RelativePath>,
    modifier: Modifier = Modifier,
    paneContent: @Composable (pane: Int, path: RelativePath, kind: TabKind) -> Unit
) {
    val workspace = state.workspace
    val paneBounds = remember { mutableStateMapOf<Int, Rect>() }
    val onDrop = { path: RelativePath, position: Offset ->
        dropTarget(
            workspace = workspace,
            path = path,
            paneBounds = paneBounds,
            position = position
        )?.let { (pane, index) ->
            onIntent(WorkspaceIntent.OnTabMove(path = path, toPane = pane, toIndex = index))
        }
        Unit
    }
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Row {
            workspace.panes.forEachIndexed { index, pane ->
                if (index > 0) PaneDivider()
                PaneColumn(
                    pane = pane,
                    index = index,
                    state = state,
                    platformConventions = platformConventions,
                    onIntent = onIntent,
                    startActions = startActions,
                    unsavedPaths = unsavedPaths,
                    onDrop = onDrop,
                    onBounds = { paneBounds[index] = it },
                    paneContent = paneContent
                )
            }
        }
    }
}

/**
 * Where the tab of [path] let go at [position] goes: the end of the other pane's tabs, or a new pane when it is let go
 * on the right edge of a single pane; null when it stays where it was.
 */
private fun dropTarget(
    workspace: Workspace,
    path: RelativePath,
    paneBounds: Map<Int, Rect>,
    position: Offset
): Pair<Int, Int>? {
    val from = workspace.find(path = path)?.first ?: return null
    val pane = paneBounds.entries.firstOrNull { (index, bounds) ->
        index in workspace.panes.indices && bounds.contains(offset = position)
    }?.key ?: return null
    return when {
        workspace.isSplit && pane != from -> pane to workspace.panes[pane].tabs.size
        workspace.isSplit -> null
        position.x > paneBounds.getValue(pane).let { it.left + it.width * SPLIT_DROP_ZONE } -> 1 to 0
        else -> null
    }
}

@Composable
private fun RowScope.PaneColumn(
    pane: EditorPane,
    index: Int,
    state: WorkspaceState,
    platformConventions: PlatformConventions,
    onIntent: (WorkspaceIntent) -> Unit,
    startActions: WorkspaceStartActions,
    unsavedPaths: Set<RelativePath>,
    onDrop: (RelativePath, Offset) -> Unit,
    onBounds: (Rect) -> Unit,
    paneContent: @Composable (pane: Int, path: RelativePath, kind: TabKind) -> Unit
) {
    Column(
        modifier = Modifier
            .weight(weight = 1f)
            .fillMaxHeight()
            .onGloballyPositioned { onBounds(it.boundsInWindow()) }
            .focusOnPress {
                if (state.workspace.activePane !=
                    index
                ) {
                    onIntent(WorkspaceIntent.OnPaneFocus(pane = index))
                }
            }
    ) {
        PaneTabs(
            pane = pane,
            index = index,
            state = state,
            platformConventions = platformConventions,
            onIntent = onIntent,
            onNewTab = startActions.onFindNote,
            unsavedPaths = unsavedPaths,
            onDrop = onDrop
        )
        Box(modifier = Modifier.weight(weight = 1f).fillMaxSize()) {
            val path = pane.activePath
            if (path ==
                null
            ) {
                NoTabs(platformConventions = platformConventions, actions = startActions)
            } else {
                paneContent(index, path, state.kindOf(path))
            }
        }
    }
}

/** A pane without any tab offers the ways to open one: find a note, a new note, today's note, search the notes. */
@Composable
private fun BoxScope.NoTabs(
    platformConventions: PlatformConventions,
    actions: WorkspaceStartActions
) {
    val keys = { shortcut: Shortcut -> shortcut.keys(operatingSystem = platformConventions.operatingSystem) }
    EmptyState(
        title = stringResource(resource = Res.string.workspace_empty_title),
        message = stringResource(resource = Res.string.workspace_empty_message),
        modifier = Modifier.align(alignment = Alignment.Center).widthIn(max = START_WIDTH_DP.dp)
    ) {
        NotekeaActionRow(
            icon = NotekeaIcons.Find,
            label = stringResource(resource = Res.string.workspace_start_find),
            keys = keys(platformConventions.quickSwitcherShortcut),
            onClick = actions.onFindNote
        )
        NotekeaActionRow(
            icon = NotekeaIcons.NewNote,
            label = stringResource(resource = Res.string.workspace_start_new),
            keys = keys(platformConventions.newNoteShortcut),
            onClick = actions.onNewNote
        )
        NotekeaActionRow(
            icon = NotekeaIcons.Today,
            label = stringResource(resource = Res.string.workspace_start_today),
            keys = keys(platformConventions.todayShortcut),
            onClick = actions.onToday
        )
        NotekeaActionRow(
            icon = NotekeaIcons.SearchNotes,
            label = stringResource(resource = Res.string.workspace_start_search),
            keys = keys(platformConventions.searchNotesShortcut),
            onClick = actions.onSearchNotes
        )
    }
}

@Composable
private fun PaneDivider() {
    Box(
        modifier = Modifier
            .width(width = NotekeaDimensions.BorderWidth)
            .fillMaxHeight()
            .background(color = MaterialTheme.colorScheme.outline)
    )
}

/** Calls [onPress] on every press inside the element, before anything inside handles it; nothing is consumed. */
private fun Modifier.focusOnPress(
    onPress: () -> Unit
): Modifier = pointerInput(onPress) {
    awaitPointerEventScope {
        while (true) {
            if (awaitPointerEvent(pass = PointerEventPass.Initial).type == PointerEventType.Press) onPress()
        }
    }
}

@Preview
@Composable
private fun WorkspaceContentPreview() {
    val notes = listOf("Recipes/Arepas.md", "Welcome.md").map { RelativePath(value = it) }
    NotekeaPreviewThemes {
        Box(modifier = Modifier.size(width = PREVIEW_WIDTH_DP.dp, height = PREVIEW_HEIGHT_DP.dp)) {
            WorkspaceContent(
                state = WorkspaceState(
                    workspace = notes.fold(initial = Workspace.EMPTY) { it, path -> it.open(path = path) }.splitRight(),
                    tree = VaultTree.EMPTY.withFolder(path = RelativePath(value = "Recipes"))
                ),
                platformConventions = PreviewPlatformConventions.linux,
                onIntent = {},
                startActions = WorkspaceStartActions(),
                unsavedPaths = emptySet()
            ) { _, path, _ -> Text(text = path.value) }
        }
    }
}

@Preview
@Composable
private fun WorkspaceContentEmptyPreview() {
    NotekeaPreviewThemes {
        Box(modifier = Modifier.size(width = PREVIEW_WIDTH_DP.dp, height = PREVIEW_HEIGHT_DP.dp)) {
            WorkspaceContent(
                state = WorkspaceState(),
                platformConventions = PreviewPlatformConventions.linux,
                onIntent = {},
                startActions = WorkspaceStartActions(),
                unsavedPaths = emptySet()
            ) { _, path, _ -> Text(text = path.value) }
        }
    }
}
