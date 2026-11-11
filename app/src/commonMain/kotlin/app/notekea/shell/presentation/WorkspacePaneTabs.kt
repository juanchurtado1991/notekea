package app.notekea.shell.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.VaultTree
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.core.presentation.designsystem.components.NotekeaIconButton
import app.notekea.core.presentation.designsystem.components.NotekeaMenuItem
import app.notekea.core.presentation.designsystem.components.NotekeaTabStrip
import app.notekea.core.presentation.designsystem.components.NotekeaTabStripItem
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.preview.PreviewPlatformConventions
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons
import app.notekea.core.presentation.platform.label
import app.notekea.resources.Res
import app.notekea.resources.tab_all
import app.notekea.resources.tab_close
import app.notekea.resources.tab_close_others
import app.notekea.resources.tab_graph
import app.notekea.resources.tab_move_to_other_pane
import app.notekea.resources.tab_new
import app.notekea.resources.tab_query
import app.notekea.resources.tab_split_right
import app.notekea.resources.tab_study
import app.notekea.resources.tab_tasks
import app.notekea.resources.tab_unsaved
import app.notekea.shell.domain.EditorPane
import app.notekea.shell.domain.Workspace
import app.notekea.tree.presentation.filepreview.fileIconOf
import app.notekea.tree.presentation.filepreview.fileTintOf
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PaneTabs(
    pane: EditorPane,
    index: Int,
    state: WorkspaceState,
    platformConventions: PlatformConventions,
    onIntent: (WorkspaceIntent) -> Unit,
    onNewTab: () -> Unit,
    unsavedPaths: Set<RelativePath>,
    onDrop: (RelativePath, Offset) -> Unit
) {
    val operatingSystem = platformConventions.operatingSystem

    val menu = tabMenu(
        state = state,
        pane = index,
        platformConventions = platformConventions,
        onIntent = onIntent
    )

    val unsaved = stringResource(resource = Res.string.tab_unsaved)

    NotekeaTabStrip(
        tabs = pane.tabs.map { path ->
            NotekeaTabStripItem(
                key = path.value,
                label = labelOf(
                    path = path,
                    state = state
                ),
                icon = iconOf(path, state),
                iconTint = fileTintOf(path = path).takeIf {
                    state.kindOf(path = path) == TabKind.FILE
                },
                unsavedLabel = unsaved.takeIf {
                    path in unsavedPaths
                }
            )
        },
        selectedIndex = pane.active,
        isActive = state.workspace.activePane == index,
        closeLabel = stringResource(resource = Res.string.tab_close),
        allTabsLabel = stringResource(resource = Res.string.tab_all),
        onSelect = {
            onIntent(
                WorkspaceIntent.OnTabSelect(
                    pane = index,
                    tab = it
                )
            )
        },
        onClose = {
            onIntent(
                WorkspaceIntent.OnTabClose(
                    pane = index,
                    tab = it
                )
            )
        },
        onDrop = { tab, position -> onDrop(pane.tabs[tab], position) },
        onReorder = { from, to ->
            onIntent(
                WorkspaceIntent.OnTabMove(
                    path = pane.tabs[from],
                    toPane = index,
                    toIndex = to
                )
            )
        },
        menuOf = menu
    ) {
        NotekeaIconButton(
            icon = NotekeaIcons.NewTab,
            label = stringResource(resource = Res.string.tab_new),
            onClick = onNewTab,
            shortcutHint = platformConventions.quickSwitcherShortcut.label(
                operatingSystem = operatingSystem
            ),
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
                    onIntent(
                        WorkspaceIntent.OnTabMove(
                            path = path,
                            toPane = 1 - pane,
                            toIndex = Int.MAX_VALUE
                        )
                    )
                }
            )
        } else {
            NotekeaMenuItem(
                label = split,
                onClick = {
                    onIntent(
                        WorkspaceIntent.OnTabMove(
                            path = path,
                            toPane = 1,
                            toIndex = 0
                        )
                    )
                },
                shortcutHint = platformConventions.splitRightShortcut.label(
                    operatingSystem = operatingSystem
                )
            )
        }

        listOf(
            listOf(
                NotekeaMenuItem(
                    label = close,
                    onClick = {
                        onIntent(
                            WorkspaceIntent.OnTabClose(
                                pane = pane,
                                tab = tab
                            )
                        )
                    },
                    shortcutHint = platformConventions.closeTabShortcut.label(
                        operatingSystem = operatingSystem
                    )
                ),
                NotekeaMenuItem(
                    label = closeOthers,
                    onClick = {
                        onIntent(
                            WorkspaceIntent.OnCloseOthers(
                                pane = pane,
                                tab = tab
                            )
                        )
                    },
                    enabled = workspace.panes[pane].tabs.size > 1
                )
            ),
            listOf(move)
        )
    }
}

@Composable
private fun labelOf(
    path: RelativePath,
    state: WorkspaceState
): String = when (state.kindOf(path = path)) {
    TabKind.NOTE -> path.nameWithoutExtension
    TabKind.GRAPH -> stringResource(resource = Res.string.tab_graph)
    TabKind.QUERY -> stringResource(resource = Res.string.tab_query)
    TabKind.TASKS -> stringResource(resource = Res.string.tab_tasks)
    TabKind.STUDY -> stringResource(resource = Res.string.tab_study)
    else -> path.name
}

private fun iconOf(
    path: RelativePath,
    state: WorkspaceState
): ImageVector = when (state.kindOf(path = path)) {
    TabKind.NOTE -> NotekeaIcons.Note
    TabKind.FOLDER -> NotekeaIcons.Folder
    TabKind.GRAPH -> NotekeaIcons.Graph
    TabKind.QUERY -> NotekeaIcons.Query
    TabKind.TASKS -> NotekeaIcons.Tasks
    TabKind.STUDY -> NotekeaIcons.Study
    TabKind.MISSING -> NotekeaIcons.MissingFile
    TabKind.FILE -> fileIconOf(path = path)
}

@Preview
@Composable
private fun PaneTabsPreview() {
    val notes = listOf("Recipes/Arepas.md", "Welcome.md").map { RelativePath(value = it) }
    val workspace = notes.fold(initial = Workspace.EMPTY) { it, path -> it.open(path = path) }

    NotekeaPreviewThemes {
        PaneTabs(
            pane = workspace.panes.first(),
            index = 0,
            state = WorkspaceState(
                workspace = workspace,
                tree = VaultTree.EMPTY
            ),
            platformConventions = PreviewPlatformConventions.linux,
            onIntent = {},
            onNewTab = {},
            unsavedPaths = setOf(notes.last()),
            onDrop = { _, _ -> }
        )
    }
}
