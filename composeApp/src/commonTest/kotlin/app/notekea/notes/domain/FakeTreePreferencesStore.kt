package app.notekea.notes.domain

/** Preferences in memory; [failure] makes reading throw, as an unexpected bug would. */
class FakeTreePreferencesStore : TreePreferencesStore {
    val saved = mutableListOf<TreePreferences>()
    var stored: TreePreferences = TreePreferences.DEFAULT
    var failure: Throwable? = null

    override suspend fun read(
        vaultPath: String
    ): TreePreferences {
        failure?.let { throw it }
        return stored
    }

    override suspend fun write(
        vaultPath: String,
        preferences: TreePreferences
    ) {
        stored = preferences
        saved += preferences
    }
}
