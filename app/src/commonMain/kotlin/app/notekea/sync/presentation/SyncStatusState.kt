package app.notekea.sync.presentation

import app.notekea.core.notes.domain.RelativePath
import app.notekea.sync.domain.SyncStatus

/**
 * The open vault's sync as the status bar shows it, the notes in [conflicts] (kept while it syncs again, so the
 * banner does not blink), and the conflict under [review].
 */
data class SyncStatusState(
    val status: SyncStatus = SyncStatus.NotConfigured,
    val conflicts: List<RelativePath> = emptyList(),
    val review: ConflictReview? = null
)
