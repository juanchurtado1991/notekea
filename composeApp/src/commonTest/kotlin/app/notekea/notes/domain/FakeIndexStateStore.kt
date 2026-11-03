package app.notekea.notes.domain

/** [IndexStateStore] in memory, per vault, as the database keeps it: a note's state replaced by its next save. */
class FakeIndexStateStore : IndexStateStore {
    val states = mutableMapOf<String, MutableMap<RelativePath, IndexedNote>>()

    override suspend fun read(
        vaultPath: String
    ): List<IndexedNote> = states[vaultPath].orEmpty().values.toList()

    override suspend fun save(
        vaultPath: String,
        note: IndexedNote
    ) {
        states.getOrPut(key = vaultPath) { mutableMapOf() }[note.file.path] = note
    }

    override suspend fun delete(
        vaultPath: String,
        path: RelativePath
    ) {
        states[vaultPath]?.remove(key = path)
    }

    override suspend fun clear(
        vaultPath: String
    ) {
        states.remove(key = vaultPath)
    }
}
