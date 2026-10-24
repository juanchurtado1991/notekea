package app.notekea.notes.domain

/** Keeps [TreePreferences] between runs: the expanded folders per vault and the sort order for every vault. */
interface TreePreferencesStore {
    suspend fun read(
        vaultPath: String
    ): TreePreferences

    suspend fun write(
        vaultPath: String,
        preferences: TreePreferences
    )
}
