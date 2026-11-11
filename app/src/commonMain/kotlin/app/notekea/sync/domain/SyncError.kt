package app.notekea.sync.domain

/** Why a sync did not go through, in the words the status bar and Settings → Sync use. */
sealed interface SyncError {
    /** The remote refused the token: Settings → Sync opens with the error. */
    data object AuthFailed : SyncError

    /** The remote could not be reached: it is tried again later, without a word. */
    data object Offline : SyncError

    /** The address is not an HTTPS (or local) Git remote. */
    data object BadAddress : SyncError

    /** Anything else Git reported, as it said it. */
    data class Failed(val message: String) : SyncError
}
