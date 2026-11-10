package app.notekea.tree.presentation.model

import app.notekea.core.notes.domain.DayNeighbors
import app.notekea.core.notes.domain.NotesError
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.VaultTree
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.tree.domain.Bookmarks
import app.notekea.tree.domain.TreePreferences
import app.notekea.tree.domain.TreeRow
import app.notekea.tree.domain.TreeSort

/**
 * State of the vault sidebar. [isLoading] covers the first scan only; later changes keep the rows on screen. [rows] are
 * [tree] flattened with [expandedFolders] and [sort]. A refused rename stays in [renaming]; [dayNeighbors] is a
 * selected daily note's date and neighbors; [dailyFolder] and [templatesFolder] are where those notes are kept.
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
    val dailyFolder: String = "",
    val templatesFolder: String = "",
    val bookmarks: Bookmarks = Bookmarks(),
    val isBookmarksExpanded: Boolean = true,
    val renaming: RenameDraft? = null,
    val pendingDelete: RelativePath? = null,
    val notice: TreeNotice? = null,
    val loadError: NotesError? = null,
    val actionError: NotesError? = null,
    val hasUnexpectedError: Boolean = false
)
