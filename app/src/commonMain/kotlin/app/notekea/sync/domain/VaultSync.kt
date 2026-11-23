package app.notekea.sync.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.flow.StateFlow

/** One vault's sync while the app runs, as the status bar and Settings → Sync drive it. */
interface VaultSync {
    val status: StateFlow<SyncStatus>

    /**
     * The files a commit left out of the repository for their size (over 100 MB, which no Git host takes) that the
     * person has not been told of yet; they stay in the vault, no longer synced.
     */
    val leftOut: StateFlow<List<RelativePath>>

    /** The person read which files were left out: [leftOut] is empty until a commit leaves out another. */
    suspend fun acknowledgeLeftOut()

    /** Starts syncing as the vault's settings say: nothing for a vault that does not sync. */
    fun start()

    fun stop()

    suspend fun syncNow()

    /** Connects the vault to the remote of [settings], keeping [token] in the keychain, and starts syncing. */
    suspend fun connect(
        settings: SyncSettings,
        token: String?
    ): Outcome<Unit, SyncError>

    /** Stops syncing: the settings and the token go; the folder and its history stay as they are. */
    suspend fun disconnect()

    suspend fun change(settings: SyncSettings)

    suspend fun resolve(
        note: RelativePath,
        choice: ConflictChoice
    )

    /** Repairs a vault left in the middle of a merge as [choice] says, then syncs. */
    suspend fun repair(choice: RepairChoice)
}
