package app.notekea.notes.presentation.tree

import app.notekea.notes.domain.RelativePath

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

    data object OnSelectNext : VaultTreeIntent

    data object OnSelectPrevious : VaultTreeIntent

    data object OnSortToggle : VaultTreeIntent
}
