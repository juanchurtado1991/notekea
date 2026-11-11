package app.notekea.sync.domain

import app.notekea.core.notes.domain.RelativePath

/** Where a vault's sync stands, as the status bar shows it. */
sealed interface SyncStatus {
    /** No remote is set: the status bar shows nothing. */
    data object NotConfigured : SyncStatus

    data object Syncing : SyncStatus

    data object UpToDate : SyncStatus

    /** [changes] files changed here and not on the remote yet (offline, or auto-sync off). */
    data class Pending(val changes: Int) : SyncStatus

    data object Offline : SyncStatus

    data object AuthFailed : SyncStatus

    /** Notes changed on both sides: each kept as it is on the remote, with ours beside it as "Note (conflict).md". */
    data class Conflict(val notes: List<RelativePath>) : SyncStatus

    data class Failed(val message: String) : SyncStatus
}
