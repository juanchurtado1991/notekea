package app.notekea.shell.data

import app.notekea.core.domain.Outcome
import app.notekea.sync.domain.RemoteAddress
import app.notekea.sync.domain.SyncError
import app.notekea.sync.domain.SyncSettings
import app.notekea.sync.domain.VaultSyncs
import app.notekea.vault.domain.VaultError
import app.notekea.vault.domain.VaultImporter

/**
 * A vault brought from a repository by the sync feature, for the welcome screen: connecting an empty folder to a
 * remote brings its notes and leaves the folder syncing with it, the token kept in the keychain as Settings → Sync
 * keeps it.
 */
class SyncVaultImporter(private val vaultSyncs: VaultSyncs) : VaultImporter {
    override suspend fun import(
        vaultPath: String,
        remoteUrl: String,
        token: String?
    ): Outcome<Unit, VaultError> {
        if (!RemoteAddress.isAddress(url = remoteUrl)) {
            return Outcome.Failure(error = VaultError.RepositoryBadAddress)
        }

        val connected = vaultSyncs
            .of(vaultPath = vaultPath)
            .connect(
                settings = SyncSettings(remoteUrl = remoteUrl),
                token = token
            )

        return when (connected) {
            is Outcome.Success -> connected
            is Outcome.Failure -> Outcome.Failure(error = problemOf(error = connected.error))
        }
    }

    private fun problemOf(error: SyncError): VaultError = when (error) {
        SyncError.AuthFailed -> VaultError.RepositoryRefused
        SyncError.Offline -> VaultError.RepositoryOffline
        SyncError.BadAddress -> VaultError.RepositoryBadAddress
        is SyncError.Failed -> VaultError.RepositoryFailed(message = error.message)
    }
}
