package app.notekea.vault.domain

import app.notekea.core.domain.Outcome
import kotlin.time.Clock

/**
 * Opens the folder at a path as a vault: checks it can be read, counts its notes and moves it to the top of the
 * recent vaults, keeping at most [MAX_RECENT_VAULTS] so the list stays short enough to scan at a glance.
 */
class OpenVaultUseCase(
    private val vaultFileSystem: VaultFileSystem,
    private val recentVaultsStore: RecentVaultsStore,
    private val clock: Clock
) {
    suspend operator fun invoke(
        path: String
    ): Outcome<OpenedVault, VaultError> = when (vaultFileSystem.access(path = path)) {
        FolderAccess.AVAILABLE -> {
            rememberAsMostRecent(path = path)
            val opened = OpenedVault(
                vault = Vault.fromPath(path = path),
                noteCount = vaultFileSystem.countNotes(path = path)
            )
            Outcome.Success(value = opened)
        }

        FolderAccess.MISSING -> {
            Outcome.Failure(error = VaultError.FolderMissing(path = path))
        }

        FolderAccess.NOT_READABLE -> {
            Outcome.Failure(error = VaultError.PermissionDenied(path = path))
        }
    }

    private suspend fun rememberAsMostRecent(
        path: String
    ) {
        val others = recentVaultsStore.read().filterNot { it.path == path }
        val opened = RecentVault(path = path, lastOpenedAt = clock.now())
        recentVaultsStore.write(recentVaults = (listOf(opened) + others).take(n = MAX_RECENT_VAULTS))
    }

    companion object {
        const val MAX_RECENT_VAULTS = 10
    }
}
