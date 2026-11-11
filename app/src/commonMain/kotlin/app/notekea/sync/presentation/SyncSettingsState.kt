package app.notekea.sync.presentation

import app.notekea.sync.domain.GitHubLimits
import app.notekea.sync.domain.SyncError
import app.notekea.sync.domain.SyncSettings
import app.notekea.sync.domain.SyncStatus

/**
 * Settings → Sync for the open vault ([hasVault]): the remote's address and a new token as typed, whether a token is
 * kept ([hasToken]), auto-sync and its interval, whether the vault syncs ([isConnected]), its [status], and the
 * [error] of the last try, and how much the vault's files take ([vaultBytes]), once measured.
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
    val error: SyncError? = null,
    val vaultBytes: Long? = null
) {
    val canConnect: Boolean get() = url.isNotBlank() && !isBusy

    /** The vault is over GitHub's 1 GB and the address, typed or connected, is on GitHub; never for a local vault. */
    val isTooLargeForGitHub: Boolean
        get() = GitHubLimits.isOver(
            url = url,
            bytes = vaultBytes
        )
}
