package app.notekea.query.domain

/** Where the Query tab's query is kept between runs, per vault. */
interface QueryPreferencesStore {
    suspend fun read(
        vaultPath: String
    ): VaultQuery

    suspend fun write(
        vaultPath: String,
        query: VaultQuery
    )
}
