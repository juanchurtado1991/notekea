package app.notekea.vault.domain

import app.notekea.core.domain.Outcome

/**
 * Brings a repository's notes into an empty folder and leaves it syncing with that repository (the sync feature's
 * work, reached through this port: features do not depend on each other). `token` is null for a repository that
 * needs none.
 */
interface VaultImporter {
    /** Whether [remoteUrl] is the address of a repository that can be brought: asked before any folder is made. */
    fun isAddress(remoteUrl: String): Boolean

    suspend fun import(
        vaultPath: String,
        remoteUrl: String,
        token: String?
    ): Outcome<Unit, VaultError>
}
