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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.components.EmptyState
import app.notekea.core.presentation.components.NotekeaIconButton
import app.notekea.core.presentation.components.NotekeaMenuItem
import app.notekea.core.presentation.components.NotekeaTabStrip
import app.notekea.core.presentation.components.NotekeaTabStripItem
import app.notekea.core.presentation.platform.label
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.preview.PreviewPlatformConventions
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons
import app.notekea.notes.domain.AttachmentKind
import app.notekea.notes.domain.EditorPane
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.VaultTree
import app.notekea.notes.domain.Workspace
import app.notekea.resources.Res
import app.notekea.resources.tab_close
import app.notekea.resources.tab_close_others
import app.notekea.resources.tab_move_to_other_pane
import app.notekea.resources.tab_new
import app.notekea.resources.tab_split_right
import app.notekea.resources.workspace_empty_message
import app.notekea.resources.workspace_empty_title
import org.jetbrains.compose.resources.stringResource

private const val PREVIEW_HEIGHT_DP = 320
private const val PREVIEW_WIDTH_DP = 900
private const val SPLIT_DROP_ZONE = 0.75f

/**
 * The editor's panes side by side (`ROADMAP.md` 1.11), each with its tabs above what its active tab shows
 * ([paneContent]: a note's editor, a file's preview, a folder's grid). A click in a pane makes it the one the keyboard
 * works in. A tab dropped on the other pane moves there; dropped on the right quarter of a single pane, it splits it
 * and moves to the new pane. The "+" after the tabs finds a note to open in a new tab ([onNewTab]). Without any tab,
 * a hint says how to open one.
 */
@Composable
fun WorkspaceContent(
    state: WorkspaceState,
    platformConventions: PlatformConventions,
    onIntent: (WorkspaceIntent) -> Unit,
    onNewTab: () -> Unit,
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
                    onNewTab = onNewTab,
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
    onNewTab: () -> Unit,
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
            onNewTab = onNewTab,
            onDrop = onDrop
        )
        Box(modifier = Modifier.weight(weight = 1f).fillMaxSize()) {
            val path = pane.activePath
            if (path ==
                null
            ) {
                NoTabs(platformConventions = platformConventions)
            } else {
                paneContent(index, path, state.kindOf(path))
            }
        }
    }
}

/** A pane without any tab says how to open one. */
@Composable
private fun BoxScope.NoTabs(
    platformConventions: PlatformConventions
) {
    val shortcut = platformConventions.quickSwitcherShortcut.label(
        operatingSystem = platformConventions.operatingSystem
    )
    EmptyState(
        title = stringResource(resource = Res.string.workspace_empty_title),
        message = stringResource(Res.string.workspace_empty_message, shortcut),
        modifier = Modifier.align(alignment = Alignment.Center)
    )
}

@Composable
private fun PaneTabs(
    pane: EditorPane,
    index: Int,
    state: WorkspaceState,
    platformConventions: PlatformConventions,
    onIntent: (WorkspaceIntent) -> Unit,
    onNewTab: () -> Unit,
    onDrop: (RelativePath, Offset) -> Unit
) {
    val operatingSystem = platformConventions.operatingSystem
    val menu = tabMenu(state = state, pane = index, platformConventions = platformConventions, onIntent = onIntent)
    NotekeaTabStrip(
        tabs = pane.tabs.map { path ->
            NotekeaTabStripItem(
                key = path.value,
                label = labelOf(path = path, state = state),
                icon = iconOf(path, state)
            )
        },
        selectedIndex = pane.active,
        isActive = state.workspace.activePane == index,
        closeLabel = stringResource(resource = Res.string.tab_close),
        onSelect = { onIntent(WorkspaceIntent.OnTabSelect(pane = index, tab = it)) },
        onClose = { onIntent(WorkspaceIntent.OnTabClose(pane = index, tab = it)) },
        onDrop = { tab, position -> onDrop(pane.tabs[tab], position) },
        menuOf = menu
    ) {
        NotekeaIconButton(
            icon = NotekeaIcons.NewTab,
            label = stringResource(resource = Res.string.tab_new),
            onClick = onNewTab,
            shortcutHint = platformConventions.quickSwitcherShortcut.label(operatingSystem = operatingSystem),
            takesFocus = false
        )
    }
}

/** A tab's menu: close it, close the others, and split right (or, with two panes, move it to the other one). */
@Composable
private fun tabMenu(
    state: WorkspaceState,
    pane: Int,
    platformConventions: PlatformConventions,
    onIntent: (WorkspaceIntent) -> Unit
): (Int) -> List<List<NotekeaMenuItem>> {
    val operatingSystem = platformConventions.operatingSystem
    val close = stringResource(resource = Res.string.tab_close)
    val closeOthers = stringResource(resource = Res.string.tab_close_others)
    val split = stringResource(resource = Res.string.tab_split_right)
    val moveToOther = stringResource(resource = Res.string.tab_move_to_other_pane)
    val workspace = state.workspace
    return { tab ->
        val path = workspace.panes[pane].tabs[tab]
        val move = if (workspace.isSplit) {
            NotekeaMenuItem(
                label = moveToOther,
                onClick = {
                    onIntent(WorkspaceIntent.OnTabMove(path = path, toPane = 1 - pane, toIndex = Int.MAX_VALUE))
                }
            )
        } else {
            NotekeaMenuItem(
                label = split,
                onClick = { onIntent(WorkspaceIntent.OnTabMove(path = path, toPane = 1, toIndex = 0)) },
                shortcutHint = platformConventions.splitRightShortcut.label(operatingSystem = operatingSystem)
            )
        }
        listOf(
            listOf(
                NotekeaMenuItem(
                    label = close,
                    onClick = { onIntent(WorkspaceIntent.OnTabClose(pane = pane, tab = tab)) },
                    shortcutHint = platformConventions.closeTabShortcut.label(operatingSystem = operatingSystem)
                ),
                NotekeaMenuItem(
                    label = closeOthers,
                    onClick = { onIntent(WorkspaceIntent.OnCloseOthers(pane = pane, tab = tab)) },
                    enabled = workspace.panes[pane].tabs.size > 1
                )
            ),
            listOf(move)
        )
    }
}

private fun labelOf(
    path: RelativePath,
    state: WorkspaceState
): String = if (state.kindOf(path = path) == TabKind.NOTE) path.nameWithoutExtension else path.name

private fun iconOf(
    path: RelativePath,
    state: WorkspaceState
): ImageVector = when (state.kindOf(path = path)) {
    TabKind.NOTE -> NotekeaIcons.Note

    TabKind.FOLDER -> NotekeaIcons.Folder

    TabKind.MISSING -> NotekeaIcons.MissingFile

    TabKind.FILE -> if (AttachmentKind.of(name = path.name) == AttachmentKind.IMAGE) {
        NotekeaIcons.ImageFile
    } else {
        NotekeaIcons.OtherFile
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
                onNewTab = {}
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
                onNewTab = {}
            ) { _, path, _ -> Text(text = path.value) }
        }
    }
}
