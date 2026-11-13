package app.notekea.shell.presentation.workspace

import androidx.compose.ui.graphics.ImageBitmap
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.VaultTree
import app.notekea.core.notes.domain.sidepanel.SidePanelPreferences
import app.notekea.shell.domain.Workspace
import app.notekea.shell.presentation.tabs.TabKind
import app.notekea.tree.presentation.filepreview.FilePreview

/**
 * State of the editor's tabs and panes. [workspace] is restored from the last run once the vault's [tree] has loaded
 * ([isRestored]), without the tabs of files that are gone. [previews] are the files shown in a tab, [covers] the
 * pictures of a folder grid's cards (null for a card without one), and [sidePanel] how the panel on the right is left.
 */
data class WorkspaceState(
    val workspace: Workspace = Workspace.EMPTY,
    val isRestored: Boolean = false,
    val tree: VaultTree = VaultTree.EMPTY,
    val previews: Map<RelativePath, FilePreview> = emptyMap(),
    val covers: Map<RelativePath, ImageBitmap?> = emptyMap(),
    val sidePanel: SidePanelPreferences = SidePanelPreferences.DEFAULT
) {
    /** What the active tab shows, as a person names it: a note without its extension; null without tabs. */
    val activeTabName: String?
        get() = workspace
            .activePath
            ?.takeIf {
                it !in Workspace.TOOL_TABS
            }
            ?.let {
                if (kindOf(path = it) == TabKind.NOTE) it.nameWithoutExtension else it.name
            }

    /** The note being read: the active pane's, or the one in the other pane, beside a tab that shows no note. */
    val noteShown: RelativePath?
        get() = (listOf(workspace.activePath) + workspace.panes.map { it.activePath })
            .firstOrNull {
                it != null && kindOf(path = it) == TabKind.NOTE
            }

    /** The note something can be written to from a tool's tab: [noteShown], or else the last one open in a tab. */
    val noteAtHand: RelativePath?
        get() = noteShown ?: workspace
            .panes
            .flatMap { it.tabs }
            .lastOrNull {
                kindOf(path = it) == TabKind.NOTE
            }

    fun kindOf(path: RelativePath): TabKind = when (path) {
        Workspace.GRAPH -> TabKind.GRAPH
        Workspace.QUERY -> TabKind.QUERY
        Workspace.TASKS -> TabKind.TASKS
        Workspace.STUDY -> TabKind.STUDY
        in tree.notes -> TabKind.NOTE
        in tree.attachments -> TabKind.FILE
        in tree.folders -> TabKind.FOLDER
        else -> if (path.isMarkdown) TabKind.NOTE else TabKind.MISSING
    }
}
