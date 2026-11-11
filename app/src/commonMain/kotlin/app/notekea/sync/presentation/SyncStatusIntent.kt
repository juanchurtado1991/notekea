package app.notekea.sync.presentation

import app.notekea.core.notes.domain.RelativePath
import app.notekea.sync.domain.ConflictChoice

sealed interface SyncStatusIntent {
    /** The status bar's entry was clicked: Settings opens at Sync. */
    data object OnOpenSettings : SyncStatusIntent

    /** Review on the banner: both versions of [note] side by side. */
    data class OnReview(val note: RelativePath) : SyncStatusIntent

    /** What to keep of the conflict under review. */
    data class OnChoose(val choice: ConflictChoice) : SyncStatusIntent

    data object OnReviewDismiss : SyncStatusIntent
}
