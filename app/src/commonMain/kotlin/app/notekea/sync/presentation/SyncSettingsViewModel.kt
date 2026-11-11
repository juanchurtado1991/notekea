package app.notekea.sync.presentation

import app.notekea.core.domain.Outcome
import app.notekea.core.presentation.BaseViewModel
import app.notekea.sync.domain.SyncError
import app.notekea.sync.domain.SyncSettings
import app.notekea.sync.domain.SyncSettingsStore
import app.notekea.sync.domain.TokenStore
import app.notekea.sync.domain.VaultSync
import app.notekea.sync.domain.VaultSyncs
import co.touchlab.kermit.Logger

/**
 * Settings → Sync (4.2) for the vault at [vaultPath], or none when no vault is open: the address and token connect it
 * through its [VaultSync], whose status it shows; auto-sync and the interval change the engine's timer at once.
 */
class SyncSettingsViewModel(
    engines: VaultSyncs,
    private val settingsStore: SyncSettingsStore,
    private val tokens: TokenStore,
    vaultPath: String?
) : BaseViewModel<SyncSettingsState, SyncSettingsIntent, Nothing>(
    initialState = SyncSettingsState(hasVault = vaultPath != null)
) {
    private val logger = Logger.withTag(tag = LOG_TAG)
    private val vault = vaultPath
    private val engine: VaultSync? = vaultPath?.let { engines.of(vaultPath = it) }

    init {
        val engine = engine

        if (vault != null && engine != null) {
            launchGuarded(onError = ::logError) { load(vaultPath = vault) }

            launchGuarded(onError = ::logError) {
                engine.status.collect { s ->
                    reduce { it.copy(status = s) }
                }
            }
        }
    }

    override fun handleIntent(intent: SyncSettingsIntent) {
        when (intent) {
            is SyncSettingsIntent.OnUrlChange -> reduce {
                it.copy(
                    url = intent.url,
                    error = null
                )
            }

            is SyncSettingsIntent.OnTokenChange -> reduce {
                it.copy(
                    token = intent.token,
                    error = null
                )
            }

            SyncSettingsIntent.OnAutoToggle -> change { it.copy(isAuto = !it.isAuto) }

            is SyncSettingsIntent.OnIntervalChoose -> change {
                it.copy(intervalMinutes = intent.minutes)
            }

            SyncSettingsIntent.OnConnect -> connect()

            SyncSettingsIntent.OnSyncNow -> engine?.let {
                launchGuarded(onError = ::logError) { it.syncNow() }
            }

            SyncSettingsIntent.OnDisconnect -> disconnect()
        }
    }

    private suspend fun load(vaultPath: String) {
        val settings = settingsStore.read(vaultPath = vaultPath) ?: return
        val hasToken = tokens.read(remoteUrl = settings.remoteUrl) != null

        reduce {
            it.copy(
                url = settings.remoteUrl,
                isAuto = settings.isAuto,
                intervalMinutes = settings.intervalMinutes,
                isConnected = true,
                hasToken = hasToken
            )
        }
    }

    private fun connect() {
        val engine = engine ?: return
        val current = state.value
        val url = current.url.trim()

        if (!isAddress(url = url)) return reduce { it.copy(error = SyncError.BadAddress) }

        reduce {
            it.copy(
                isBusy = true,
                error = null
            )
        }

        launchGuarded(onError = ::logError) {
            val settings = SyncSettings(
                remoteUrl = url,
                isAuto = current.isAuto,
                intervalMinutes = current.intervalMinutes
            )

            val connected = engine.connect(
                settings = settings,
                token = current.token.ifBlank { null }
            )

            val hasToken = tokens.read(remoteUrl = url) != null

            reduce {
                it.copy(
                    isBusy = false,
                    token = "",
                    hasToken = hasToken,
                    isConnected = connected is Outcome.Success || it.isConnected,
                    error = (connected as? Outcome.Failure)?.error
                )
            }
        }
    }

    private fun change(transform: (SyncSettingsState) -> SyncSettingsState) {
        reduce(transform)
        val engine = engine ?: return
        val current = state.value
        if (!current.isConnected) return

        launchGuarded(onError = ::logError) {
            // The address it is connected to: one typed in the field counts only once Connect is clicked.
            val saved = vault?.let { settingsStore.read(vaultPath = it) } ?: return@launchGuarded

            engine.change(
                settings = saved.copy(
                    isAuto = current.isAuto,
                    intervalMinutes = current.intervalMinutes
                )
            )
        }
    }

    private fun disconnect() {
        val engine = engine ?: return

        launchGuarded(onError = ::logError) {
            engine.disconnect()
            reduce { SyncSettingsState(hasVault = true) }
        }
    }

    private fun isAddress(url: String): Boolean = ADDRESS_PREFIXES.any {
        url.startsWith(
            prefix = it,
            ignoreCase = true
        )
    }

    private fun logError(throwable: Throwable) {
        logger.w(throwable = throwable) { FAILED_MESSAGE }
    }

    private companion object {
        const val LOG_TAG = "SyncSettingsViewModel"
        const val FAILED_MESSAGE = "Sync settings failed"

        // HTTPS for a real remote; a local one (file:) for trying sync with a folder on this computer.
        val ADDRESS_PREFIXES = listOf("https://", "file:")
    }
}
