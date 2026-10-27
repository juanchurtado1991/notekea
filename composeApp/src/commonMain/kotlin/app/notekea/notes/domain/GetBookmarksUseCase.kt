package app.notekea.notes.domain

/** The vault's bookmarks as they were left. */
class GetBookmarksUseCase(private val bookmarkStore: BookmarkStore) {
    suspend operator fun invoke(
        vaultPath: String
    ): Bookmarks = bookmarkStore.read(vaultPath = vaultPath)
}
