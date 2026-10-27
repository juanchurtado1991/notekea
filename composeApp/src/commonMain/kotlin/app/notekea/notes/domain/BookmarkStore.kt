package app.notekea.notes.domain

/**
 * Keeps each vault's bookmarks between runs (`ROADMAP.md` 1.13), in Notekea's own settings, never in the vault: its
 * files stay untouched and Obsidian's bookmarks are left alone.
 */
interface BookmarkStore {
    /** The vault's bookmarks, none when it has none saved. */
    suspend fun read(
        vaultPath: String
    ): Bookmarks

    suspend fun write(
        vaultPath: String,
        bookmarks: Bookmarks
    )
}
