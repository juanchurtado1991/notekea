package app.notekea.notes.presentation.tree

import app.notekea.notes.domain.RelativePath

/** What the user did with bookmarks (`ROADMAP.md` 1.13): in the sidebar, the note's header, a menu or `Mod+Shift+B`. */
sealed interface BookmarkIntent : VaultTreeIntent {
    /** Bookmarks [path] (a note, a folder or another file), or removes its bookmark when it has one. */
    data class OnToggle(val path: RelativePath) : BookmarkIntent

    /** The bookmark at [from] was dragged to [to] among the others. */
    data class OnMove(
        val from: Int,
        val to: Int
    ) : BookmarkIntent

    /** Remove on a bookmark whose entry is gone, or on any bookmark's menu. */
    data class OnRemove(val path: RelativePath) : BookmarkIntent

    /** A bookmark was clicked: its entry is selected in the tree and opens in a tab. */
    data class OnClick(val path: RelativePath) : BookmarkIntent

    /** The Bookmarks section's header: folds or unfolds the list. */
    data object OnSectionToggle : BookmarkIntent
}
