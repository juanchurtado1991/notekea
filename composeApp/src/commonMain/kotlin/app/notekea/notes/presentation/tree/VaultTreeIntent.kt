package app.notekea.notes.presentation.tree

import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.TreeSort

/** What the user did in the vault sidebar. Actions without a path apply to the selected entry. */
sealed interface VaultTreeIntent {
    data class OnChevronClick(val path: RelativePath) : VaultTreeIntent

    data object OnCollapseSelected : VaultTreeIntent

    data object OnDeleteCancel : VaultTreeIntent

    data object OnDeleteConfirm : VaultTreeIntent

    data class OnDeleteRequest(val path: RelativePath? = null) : VaultTreeIntent

    data object OnErrorDismiss : VaultTreeIntent

    data object OnExpandSelected : VaultTreeIntent

    /** [baseName] is the localized name of a new folder ("Untitled folder"). */
    data class OnNewFolderClick(val baseName: String) : VaultTreeIntent

    /** [baseName] is the localized name of a new note ("Untitled"). */
    data class OnNewNoteClick(val baseName: String) : VaultTreeIntent

    data object OnNoticeDismiss : VaultTreeIntent

    data object OnRenameCancel : VaultTreeIntent

    data object OnRenameConfirm : VaultTreeIntent

    data class OnRenameRequest(val path: RelativePath? = null) : VaultTreeIntent

    data class OnRenameTextChange(val text: String) : VaultTreeIntent

    data object OnRetryClick : VaultTreeIntent

    data class OnRevealRequest(val path: RelativePath? = null) : VaultTreeIntent

    data class OnRowClick(val path: RelativePath) : VaultTreeIntent

    /** Selects without opening or closing a folder (a right click before its menu). */
    data class OnRowSelect(val path: RelativePath) : VaultTreeIntent

    /** A click or right click on the empty part of the tree: nothing is selected, so new entries go to the root. */
    data object OnSelectionClear : VaultTreeIntent

    data object OnSelectNext : VaultTreeIntent

    /** The user finished dragging the sidebar edge to [widthDp]. */
    data class OnSidebarResize(val widthDp: Int) : VaultTreeIntent

    data object OnSidebarToggle : VaultTreeIntent

    data object OnSelectPrevious : VaultTreeIntent

    data class OnSortChange(val sort: TreeSort) : VaultTreeIntent
}
