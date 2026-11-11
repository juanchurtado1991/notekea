package app.notekea.sync.domain

/** The [VaultSync] of each vault, the same one for the app's life. */
fun interface VaultSyncs {
    fun of(vaultPath: String): VaultSync
}
