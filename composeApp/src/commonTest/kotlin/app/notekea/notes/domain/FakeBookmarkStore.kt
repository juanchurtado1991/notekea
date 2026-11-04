package app.notekea.notes.domain

import app.notekea.core.notes.domain.Gate

/** Bookmarks in memory, per vault, as the settings would keep them; every call goes through [gate]. */
class FakeBookmarkStore : BookmarkStore {
    val saved = mutableMapOf<String, Bookmarks>()
    val gate = Gate()

    override suspend fun read(
        vaultPath: String
    ): Bookmarks {
        gate.pass()
        return saved[vaultPath] ?: Bookmarks()
    }

    override suspend fun write(
        vaultPath: String,
        bookmarks: Bookmarks
    ) {
        gate.pass()
        saved[vaultPath] = bookmarks
    }
}
