package app.notekea.notes.presentation.workspace

import androidx.compose.ui.graphics.ImageBitmap
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.SidePanelPreferences
import app.notekea.notes.domain.VaultTree
import app.notekea.notes.domain.Workspace
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
    fun kindOf(
        path: RelativePath
    ): TabKind = when (path) {
        in tree.notes -> TabKind.NOTE
        in tree.attachments -> TabKind.FILE
        in tree.folders -> TabKind.FOLDER
        else -> if (path.isMarkdown) TabKind.NOTE else TabKind.MISSING
    }
}
