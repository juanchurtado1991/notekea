package app.notekea.notes.domain

/** The cache in memory, per vault, recording every update. */
class FakeNoteMetadataCache : NoteMetadataCache {
    val entries = mutableMapOf<RelativePath, NoteEntry>()
    val updates = mutableListOf<Pair<List<RelativePath>, List<RelativePath>>>()

    override suspend fun read(
        vaultPath: String
    ): List<NoteEntry> = entries.values.toList()

    override suspend fun update(
        vaultPath: String,
        upserted: List<NoteEntry>,
        removed: List<RelativePath>
    ) {
        updates += upserted.map { it.file.path } to removed
        removed.forEach { entries.remove(it) }
        upserted.forEach { entries[it.file.path] = it }
    }
}
