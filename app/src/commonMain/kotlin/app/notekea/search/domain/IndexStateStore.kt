package app.notekea.search.domain

import app.notekea.core.notes.domain.RelativePath

/** Where the search index keeps what it holds of each note, per vault, in the app's data. */
interface IndexStateStore {
    suspend fun read(vaultPath: String): List<IndexedNote>

    suspend fun save(
        vaultPath: String,
        note: IndexedNote
    )

    suspend fun delete(
        vaultPath: String,
        path: RelativePath
    )

    /** Forgets every note of the vault, so the next run indexes them all (Rebuild index); false when it could not. */
    suspend fun clear(vaultPath: String): Boolean
}
