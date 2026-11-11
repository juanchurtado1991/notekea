package app.notekea.sync.presentation

import app.notekea.sync.domain.SyncStatus

/** The open vault's sync as the status bar and the conflict banner show it, and the conflict under [review]. */
data class SyncStatusState(
    val status: SyncStatus = SyncStatus.NotConfigured,
    val review: ConflictReview? = null
)
