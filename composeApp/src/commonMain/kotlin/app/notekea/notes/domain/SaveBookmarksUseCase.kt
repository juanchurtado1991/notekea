package app.notekea.notes.domain

/** Remembers the vault's [bookmarks] for the next run. */
class SaveBookmarksUseCase(private val bookmarkStore: BookmarkStore) {
    suspend operator fun invoke(
        vaultPath: String,
        bookmarks: Bookmarks
    ) = bookmarkStore.write(vaultPath = vaultPath, bookmarks = bookmarks)
}
