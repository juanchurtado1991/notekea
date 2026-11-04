package app.notekea.search.domain

import app.notekea.core.notes.domain.RelativePath

/**
 * [IndexStateStore] in memory, per vault, as the database keeps it: a note's state replaced by its next save. With
 * [isClearRefused], clearing fails as a locked database does.
 */
class FakeIndexStateStore : IndexStateStore {
    val states = mutableMapOf<String, MutableMap<RelativePath, IndexedNote>>()
    var isClearRefused = false

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
    ): Boolean {
        if (isClearRefused) return false
        states.remove(key = vaultPath)
        return true
    }
}
