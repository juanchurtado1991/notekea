package app.notekea.vault.domain

import app.notekea.core.domain.time.DescribeElapsedTimeUseCase

/** Lists the recent vaults, most recent first, marking the ones whose folder no longer exists. */
class GetRecentVaultsUseCase(
    private val recentVaultsStore: RecentVaultsStore,
    private val vaultFileSystem: VaultFileSystem,
    private val describeElapsedTime: DescribeElapsedTimeUseCase
) {
    suspend operator fun invoke(): List<RecentVaultStatus> = recentVaultsStore.read().map { recent ->
        RecentVaultStatus(
            vault = Vault.fromPath(path = recent.path),
            lastOpened = describeElapsedTime(since = recent.lastOpenedAt),
            isMissing = vaultFileSystem.access(path = recent.path) == FolderAccess.MISSING
        )
    }
}
