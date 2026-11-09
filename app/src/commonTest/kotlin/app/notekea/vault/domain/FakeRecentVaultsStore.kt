package app.notekea.vault.domain

/** In-memory [RecentVaultsStore]. */
class FakeRecentVaultsStore : RecentVaultsStore {
    var recentVaults: List<RecentVault> = emptyList()

    override suspend fun read(): List<RecentVault> = recentVaults

    override suspend fun write(
        recentVaults: List<RecentVault>
    ) {
        this.recentVaults = recentVaults
    }
}
