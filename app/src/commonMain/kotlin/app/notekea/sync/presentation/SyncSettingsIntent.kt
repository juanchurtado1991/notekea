package app.notekea.sync.presentation

sealed interface SyncSettingsIntent {
    data class OnUrlChange(val url: String) : SyncSettingsIntent

    data class OnTokenChange(val token: String) : SyncSettingsIntent

    data object OnAutoToggle : SyncSettingsIntent

    data class OnIntervalChoose(val minutes: Int) : SyncSettingsIntent

    /** Connect, or save a changed address or token: the vault syncs with the remote at once. */
    data object OnConnect : SyncSettingsIntent

    data object OnSyncNow : SyncSettingsIntent

    /** Stop syncing: the settings and the token go, the notes and their history stay. */
    data object OnDisconnect : SyncSettingsIntent
}
