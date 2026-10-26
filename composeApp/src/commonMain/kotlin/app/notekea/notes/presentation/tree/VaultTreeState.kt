package app.notekea.notes.presentation.tree

import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.TreeRow
import app.notekea.notes.domain.TreePreferences
import app.notekea.notes.domain.TreeSort
import app.notekea.notes.domain.VaultTree

/**
 * State of the vault sidebar. [isLoading] covers the first scan only; later changes keep the rows on screen.
 * [rows] are [tree] flattened with [expandedFolders] and [sort]. [loadError] means the vault could not be read (retry
 * scans again); [actionError] is a failed create, delete or reveal, shown in a dismissible banner; a refused rename
 * stays in [renaming], next to the field. [notice] reports a finished action
 * (links updated after a rename). [sidebarWidthDp] changes when a resize ends, not on every dragged frame. A selected
 * file that is not a note is shown beside the tree as [filePreview].
 */
data class VaultTreeState(
    val vaultPath: String,
    val platformConventions: PlatformConventions,
    val isLoading: Boolean = true,
    val tree: VaultTree = VaultTree.EMPTY,
    val rows: List<TreeRow> = emptyList(),
    val expandedFolders: Set<RelativePath> = emptySet(),
    val sort: TreeSort = TreeSort.NAME,
    val sidebarWidthDp: Int = TreePreferences.DEFAULT_SIDEBAR_WIDTH_DP,
    val isSidebarCollapsed: Boolean = false,
    val selectedPath: RelativePath? = null,
    val filePreview: FilePreview? = null,
    val renaming: RenameDraft? = null,
    val pendingDelete: RelativePath? = null,
    val notice: TreeNotice? = null,
    val loadError: NotesError? = null,
    val actionError: NotesError? = null,
    val hasUnexpectedError: Boolean = false
)
