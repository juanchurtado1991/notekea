package app.notekea.notes.domain

/** Bookmarks in memory, per vault, as the settings would keep them. */
class FakeBookmarkStore : BookmarkStore {
    val saved = mutableMapOf<String, Bookmarks>()

    override suspend fun read(
        vaultPath: String
    ): Bookmarks = saved[vaultPath] ?: Bookmarks()

    override suspend fun write(
        vaultPath: String,
        bookmarks: Bookmarks
    ) {
        saved[vaultPath] = bookmarks
    }
}
