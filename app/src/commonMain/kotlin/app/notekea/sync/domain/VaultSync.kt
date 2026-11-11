package app.notekea.sync.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.notes.domain.RelativePath
import kotlinx.coroutines.flow.StateFlow

/** One vault's sync while the app runs, as the status bar and Settings → Sync drive it. */
interface VaultSync {
    val status: StateFlow<SyncStatus>

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
}
