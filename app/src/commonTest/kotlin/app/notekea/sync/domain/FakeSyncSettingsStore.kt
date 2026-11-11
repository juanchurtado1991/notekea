package app.notekea.sync.domain

/** Each vault's sync settings, in memory. */
class FakeSyncSettingsStore : SyncSettingsStore {
    val settings = mutableMapOf<String, SyncSettings>()

    override suspend fun read(vaultPath: String): SyncSettings? = settings[vaultPath]

    override suspend fun write(
        vaultPath: String,
        settings: SyncSettings?
    ) {
        if (settings == null) this.settings -= vaultPath else this.settings[vaultPath] = settings
    }
}
