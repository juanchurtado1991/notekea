package app.notekea.notes.domain

/** What a key does in a list item, as in other editors (`ROADMAP.md` 1.17); see [ListEditing]. */
enum class ListAction {
    /** Enter: a new item of the same list, or the end of the list on an empty item. */
    NEW_ITEM,

    /** Tab: the item becomes a child of the item above it. */
    INDENT,

    /** Shift+Tab: the item goes back to the level of its parent. */
    OUTDENT,

    /** Backspace right after the marker: the line stops being a list item. */
    REMOVE_MARKER
}
