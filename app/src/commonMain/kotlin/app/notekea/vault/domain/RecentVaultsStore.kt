package app.notekea.vault.domain

/** Persists the recent vaults between runs, most recent first. */
interface RecentVaultsStore {
    suspend fun read(): List<RecentVault>

    suspend fun write(
        recentVaults: List<RecentVault>
    )
}
