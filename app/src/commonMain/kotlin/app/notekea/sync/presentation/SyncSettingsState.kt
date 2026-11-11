package app.notekea.sync.presentation

import app.notekea.sync.domain.SyncError
import app.notekea.sync.domain.SyncSettings
import app.notekea.sync.domain.SyncStatus

/**
 * Settings → Sync for the open vault ([hasVault]): the remote's address and a new token as typed, whether a token is
 * kept ([hasToken]), auto-sync and its interval, whether the vault syncs ([isConnected]), its [status], and the
 * [error] of the last try.
 */
data class SyncSettingsState(
    val hasVault: Boolean = false,
    val url: String = "",
    val token: String = "",
    val hasToken: Boolean = false,
    val isAuto: Boolean = true,
    val intervalMinutes: Int = SyncSettings.DEFAULT_INTERVAL_MINUTES,
    val isConnected: Boolean = false,
    val isBusy: Boolean = false,
    val status: SyncStatus = SyncStatus.NotConfigured,
    val error: SyncError? = null
) {
    val canConnect: Boolean get() = url.isNotBlank() && !isBusy
}
