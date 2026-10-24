package app.notekea.notes.domain

/**
 * What was last read from the notes of a vault, kept between runs so a vault opens at once. It is only a cache: a
 * cache that cannot be read answers as empty, and the notes are read from disk again.
 */
interface NoteMetadataCache {
    suspend fun read(
        vaultPath: String
    ): List<NoteEntry>

    /** Stores [upserted] (new or changed notes) and forgets [removed], in one transaction. */
    suspend fun update(
        vaultPath: String,
        upserted: List<NoteEntry>,
        removed: List<RelativePath>
    )
}
