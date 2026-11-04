package app.notekea.shell.presentation

import androidx.compose.ui.graphics.ImageBitmap
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.SidePanelPreferences
import app.notekea.notes.domain.VaultTree
import app.notekea.shell.domain.Workspace
import app.notekea.notes.presentation.tree.FilePreview

/**
 * State of the editor's tabs and panes (`ROADMAP.md` 1.11). [workspace] is restored from the last run once the vault's
 * [tree] has loaded ([isRestored]), without the tabs of files that are gone. [previews] are the files shown in a tab,
 * [covers] the pictures of a folder grid's cards (null for a card without one), and [sidePanel] how the panel on the
 * right is left (`ROADMAP.md` 1.9).
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
        get() = workspace.activePath?.let {
            if (kindOf(path = it) == TabKind.NOTE) it.nameWithoutExtension else it.name
        }

    fun kindOf(
        path: RelativePath
    ): TabKind = when (path) {
        in tree.notes -> TabKind.NOTE
        in tree.attachments -> TabKind.FILE
        in tree.folders -> TabKind.FOLDER
        else -> if (path.isMarkdown) TabKind.NOTE else TabKind.MISSING
    }
}
