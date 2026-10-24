package app.notekea.notes.domain

import kotlinx.coroutines.flow.Flow

/**
 * Emits what changed inside a vault while it is watched: notes and folders created, modified or deleted, with paths
 * relative to the vault. Platform implementations live outside `commonMain` (directory-watcher on desktop).
 */
interface FileWatcher {
    fun watch(
        vaultPath: String
    ): Flow<VaultChange>
}
