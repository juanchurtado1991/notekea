package app.notekea.tree.presentation

import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.tree.domain.Bookmarks
import app.notekea.core.notes.domain.DayNeighbors
import app.notekea.core.notes.domain.NotesError
import app.notekea.core.notes.domain.RelativePath
import app.notekea.tree.domain.TreeRow
import app.notekea.tree.domain.TreePreferences
import app.notekea.tree.domain.TreeSort
import app.notekea.core.notes.domain.VaultTree

/**
 * State of the vault sidebar. [isLoading] covers the first scan only; later changes keep the rows on screen.
 * [rows] are [tree] flattened with [expandedFolders] and [sort]. [loadError] means the vault could not be read (retry
 * scans again); [actionError] is a failed create, delete or reveal, shown in a dismissible banner; a refused rename
 * stays in [renaming], next to the field. [notice] reports a finished action
 * (links updated after a rename). [sidebarWidthDp] changes when a resize ends, not on every dragged frame. When the
 * selected note is a daily note,
 * [dayNeighbors] holds its date and the daily notes before and after it (`ROADMAP.md` 1.10). [bookmarks] are the
 * vault's bookmarks, listed above the tree while [isBookmarksExpanded] (`ROADMAP.md` 1.13).
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
    val dayNeighbors: DayNeighbors? = null,
    val bookmarks: Bookmarks = Bookmarks(),
    val isBookmarksExpanded: Boolean = true,
    val renaming: RenameDraft? = null,
    val pendingDelete: RelativePath? = null,
    val notice: TreeNotice? = null,
    val loadError: NotesError? = null,
    val actionError: NotesError? = null,
    val hasUnexpectedError: Boolean = false
)
