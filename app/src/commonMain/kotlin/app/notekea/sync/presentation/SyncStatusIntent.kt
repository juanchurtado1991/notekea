package app.notekea.sync.presentation

import app.notekea.core.notes.domain.RelativePath
import app.notekea.sync.domain.ConflictChoice
import app.notekea.sync.domain.RepairChoice

sealed interface SyncStatusIntent {
    /** The status bar's entry was clicked: Settings opens at Sync. */
    data object OnOpenSettings : SyncStatusIntent

    /** Review on the banner: both versions of [note] side by side. */
    data class OnReview(val note: RelativePath) : SyncStatusIntent

    /** What to keep of the conflict under review. */
    data class OnChoose(val choice: ConflictChoice) : SyncStatusIntent

    data object OnReviewDismiss : SyncStatusIntent

    /** Repair on the banner of a vault left in the middle of a merge: the two ways to put it right. */
    data object OnRepairShow : SyncStatusIntent

    data object OnRepairDismiss : SyncStatusIntent

    data class OnRepair(val choice: RepairChoice) : SyncStatusIntent

    /** OK on the dialog naming the files left out for their size. */
    data object OnLeftOutDismiss : SyncStatusIntent
}
