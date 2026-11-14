package app.notekea.vault.domain

import app.notekea.core.domain.Outcome

/**
 * Brings a repository's notes into an empty folder and leaves it syncing with that repository (the sync feature's
 * work, reached through this port: features do not depend on each other). `token` is null for a repository that
 * needs none.
 */
fun interface VaultImporter {
    suspend fun import(
        vaultPath: String,
        remoteUrl: String,
        token: String?
    ): Outcome<Unit, VaultError>
}
