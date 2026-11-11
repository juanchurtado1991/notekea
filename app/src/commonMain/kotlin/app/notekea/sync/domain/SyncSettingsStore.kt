package app.notekea.sync.domain

/** Each vault's [SyncSettings]; null when the vault does not sync. */
interface SyncSettingsStore {
    suspend fun read(vaultPath: String): SyncSettings?

    suspend fun write(
        vaultPath: String,
        settings: SyncSettings?
    )
}
