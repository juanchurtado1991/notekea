package app.notekea.vault.domain

/** Forgets a recent vault. The folder itself is never touched. */
class RemoveRecentVaultUseCase(private val recentVaultsStore: RecentVaultsStore) {
    suspend operator fun invoke(
        path: String
    ) {
        recentVaultsStore.write(recentVaults = recentVaultsStore.read().filterNot { it.path == path })
    }
}
