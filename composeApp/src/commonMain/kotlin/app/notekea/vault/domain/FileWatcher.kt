package app.notekea.vault.domain

import kotlinx.coroutines.flow.Flow

/**
 * Emits the paths of Markdown files that changed inside the vault, so only those are re-indexed.
 * Platform implementations live outside `commonMain` (directory-watcher on desktop).
 */
interface FileWatcher {
    fun watch(
        vaultPath: String
    ): Flow<String>
}
