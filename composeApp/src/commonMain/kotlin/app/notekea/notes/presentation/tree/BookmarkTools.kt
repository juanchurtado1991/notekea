package app.notekea.notes.presentation.tree

import app.notekea.notes.domain.GetBookmarksUseCase
import app.notekea.notes.domain.SaveBookmarksUseCase

/** What the sidebar needs for bookmarks (`ROADMAP.md` 1.13), given together so its constructor stays short. */
class BookmarkTools(
    val getBookmarks: GetBookmarksUseCase,
    val saveBookmarks: SaveBookmarksUseCase
)
