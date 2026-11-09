package app.notekea.graph.domain

/** Where [GraphPreferences] are kept, per vault. */
interface GraphPreferencesStore {
    suspend fun read(vaultPath: String): GraphPreferences

    suspend fun write(
        vaultPath: String,
        preferences: GraphPreferences
    )
}
