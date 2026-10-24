package app.notekea.notes.domain

/** How the vault tree orders the notes of a folder; folders always come first, by name. */
enum class TreeSort {
    MODIFIED,
    NAME
}
