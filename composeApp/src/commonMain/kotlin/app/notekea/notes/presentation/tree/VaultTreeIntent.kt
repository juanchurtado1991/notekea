package app.notekea.notes.presentation.tree

import app.notekea.notes.domain.LinkKind
import app.notekea.notes.domain.LinkUpdateResult
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.TreeSort

/** What the user did in the vault sidebar. Actions without a path apply to the selected entry. */
sealed interface VaultTreeIntent {
    data class OnChevronClick(val path: RelativePath) : VaultTreeIntent

    data object OnCollapseSelected : VaultTreeIntent

    data object OnDeleteCancel : VaultTreeIntent

    data object OnDeleteConfirm : VaultTreeIntent

    data class OnDeleteRequest(val path: RelativePath? = null) : VaultTreeIntent

    /** The note editor renamed [from] to [to] and updated the links to it as [links] says. */
    data class OnEntryRenamed(
        val from: RelativePath,
        val to: RelativePath,
        val links: LinkUpdateResult?
    ) : VaultTreeIntent

    data object OnErrorDismiss : VaultTreeIntent

    data object OnExpandSelected : VaultTreeIntent

    /** A link in the open note [source] was clicked: select the note it leads to, creating a missing wiki link's. */
    data class OnFollowLink(
        val source: RelativePath,
        val target: String,
        val kind: LinkKind
    ) : VaultTreeIntent

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

    /** A note chosen outside the tree (the quick switcher): it is selected, with the folders above it expanded. */
    data class OnRevealNote(val path: RelativePath) : VaultTreeIntent

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
