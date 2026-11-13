package app.notekea.shell.presentation.follow

import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.ActionStep
import app.notekea.core.notes.domain.sidepanel.SidePanelMode
import app.notekea.editor.presentation.model.EditorIntent
import app.notekea.search.presentation.switcher.QuickSwitcherIntent
import app.notekea.search.presentation.switcher.QuickSwitcherMode
import app.notekea.shell.domain.Workspace
import app.notekea.shell.presentation.tabs.openGraph
import app.notekea.shell.presentation.workspace.PaneEditors
import app.notekea.shell.presentation.workspace.WorkspaceIntent
import app.notekea.shell.presentation.workspace.WorkspaceViewModels
import app.notekea.tree.presentation.model.VaultTreeIntent

/**
 * Does in the window what a chat step only shows (3.9), as the person would with the rail, the tabs and the side panel:
 * open a note or today's, search, show a panel or a tool, split, close the other tabs, or the note as written.
 */
internal fun showStep(
    step: ActionStep,
    viewModels: WorkspaceViewModels,
    editors: PaneEditors
) {
    val workspace = viewModels.workspace

    when (step.kind) {
        ActionKind.OPEN_NOTE ->
            step
                .notes
                .firstOrNull()
                ?.let { workspace.onIntent(intent = WorkspaceIntent.OnOpen(path = it)) }

        ActionKind.OPEN_TODAY -> viewModels.tree.onIntent(intent = VaultTreeIntent.OnTodayClick)

        ActionKind.SEARCH -> viewModels.onSwitcher(
            QuickSwitcherIntent.OnOpen(
                mode = QuickSwitcherMode.CONTENT,
                query = step.text.orEmpty()
            )
        )

        ActionKind.SHOW_PANEL -> showPanel(
            panel = step
                .panel
                .orEmpty()
                .lowercase(),
            viewModels = viewModels
        )

        ActionKind.SPLIT_PANE -> workspace.onIntent(intent = WorkspaceIntent.OnSplitRight)

        ActionKind.CLOSE_OTHER_TABS -> {
            val tabs = workspace.state.value.workspace
            val pane = tabs.activePane

            val tab = tabs
                .panes
                .getOrNull(index = pane)
                ?.active
                ?.takeIf {
                    it >= 0
                } ?: return

            workspace.onIntent(
                intent = WorkspaceIntent.OnCloseOthers(
                    pane = pane,
                    tab = tab
                )
            )
        }

        ActionKind.TOGGLE_SOURCE ->
            editors.active.onIntent(intent = EditorIntent.OnSourceModeToggle)

        else -> Unit
    }
}

private fun showPanel(
    panel: String,
    viewModels: WorkspaceViewModels
) {
    val workspace = viewModels.workspace

    when (panel) {
        "files", "bookmarks" -> viewModels.tree.onIntent(intent = VaultTreeIntent.OnSidebarToggle)

        "outline" -> workspace.onIntent(intent = WorkspaceIntent.OnOutlineFocusRequest)

        "chat" -> workspace.onIntent(intent = WorkspaceIntent.OnChatFocusRequest)

        "graph" -> openGraph(viewModels = viewModels)

        "tasks" -> workspace.onIntent(intent = WorkspaceIntent.OnOpen(path = Workspace.TASKS))

        "query" -> workspace.onIntent(intent = WorkspaceIntent.OnOpen(path = Workspace.QUERY))

        else -> workspace.onIntent(
            intent = WorkspaceIntent.OnSidePanelModeSelect(mode = SidePanelMode.INSPECTOR)
        )
    }
}
