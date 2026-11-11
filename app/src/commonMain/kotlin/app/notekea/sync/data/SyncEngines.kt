package app.notekea.sync.data

import app.notekea.core.domain.AppDispatchers
import app.notekea.sync.domain.GitRepository
import app.notekea.sync.domain.SyncSettingsStore
import app.notekea.sync.domain.TokenStore
import app.notekea.sync.domain.VaultEdits
import app.notekea.sync.domain.VaultSync
import app.notekea.sync.domain.VaultSyncs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * The [SyncEngine] of each vault, one for the app's life, so the status bar of the open vault and Settings → Sync,
 * which opens over any window, share it.
 */
class SyncEngines(
    private val repository: GitRepository,
    private val tokens: TokenStore,
    private val settingsStore: SyncSettingsStore,
    private val edits: VaultEdits,
    dispatchers: AppDispatchers
) : VaultSyncs {
    private val scope = CoroutineScope(context = SupervisorJob() + dispatchers.io)
    private val engines = mutableMapOf<String, VaultSync>()

    override fun of(vaultPath: String): VaultSync = engines
        .getOrPut(key = vaultPath) {
            SyncEngine(
                vaultPath = vaultPath,
                repository = repository,
                tokens = tokens,
                settingsStore = settingsStore,
                edits = edits,
                scope = scope
            )
        }
}
