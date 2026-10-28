package app.notekea.notes.presentation.tree

import app.notekea.notes.domain.BookmarkStore
import app.notekea.notes.domain.Bookmarks
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * The vault's bookmarks (`ROADMAP.md` 1.13): read when the vault opens, changed by the sidebar, the star and the
 * renames and deletions of their entries, and saved after every change, one save at a time.
 */
internal class TreeBookmarks(
    private val host: TreeHost,
    private val vaultPath: String,
    private val bookmarkStore: BookmarkStore
) {
    private val saving = Mutex()

    suspend fun load() {
        val bookmarks = bookmarkStore.read(vaultPath = vaultPath)
        host.reduce { it.copy(bookmarks = bookmarks) }
    }

    fun onIntent(
        intent: BookmarkIntent
    ) {
        when (intent) {
            is BookmarkIntent.OnToggle -> change { it.toggled(path = intent.path) }
            is BookmarkIntent.OnMove -> change { it.moved(from = intent.from, to = intent.to) }
            is BookmarkIntent.OnRemove -> change { it.without(path = intent.path) }
            is BookmarkIntent.OnClick -> host.reveal(path = intent.path)
            BookmarkIntent.OnSectionToggle -> host.reduce { it.copy(isBookmarksExpanded = !it.isBookmarksExpanded) }
        }
    }

    /** Changes the bookmarks and saves them, one save at a time, each writing the latest ones. */
    fun change(
        transform: (Bookmarks) -> Bookmarks
    ) {
        val before = host.state.value.bookmarks
        host.reduce { it.copy(bookmarks = transform(it.bookmarks)) }
        if (host.state.value.bookmarks == before) return
        host.launch {
            saving.withLock { bookmarkStore.write(vaultPath = vaultPath, bookmarks = host.state.value.bookmarks) }
        }
    }
}
