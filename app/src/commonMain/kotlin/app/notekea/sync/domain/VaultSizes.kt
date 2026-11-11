package app.notekea.sync.domain

/** How much a vault's files take, its Git history left out: what a remote of it holds. */
fun interface VaultSizes {
    suspend fun bytesOf(vaultPath: String): Long
}
