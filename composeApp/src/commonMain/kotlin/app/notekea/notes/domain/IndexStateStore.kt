package app.notekea.notes.domain

/** Where the search index keeps what it holds of each note, per vault (`ROADMAP.md` 2.3), in the app's data. */
interface IndexStateStore {
    suspend fun read(
        vaultPath: String
    ): List<IndexedNote>

    suspend fun save(
        vaultPath: String,
        note: IndexedNote
    )

    suspend fun delete(
        vaultPath: String,
        path: RelativePath
    )

    /** Forgets every note of the vault, so the next run indexes them all (Rebuild index). */
    suspend fun clear(
        vaultPath: String
    )
}
