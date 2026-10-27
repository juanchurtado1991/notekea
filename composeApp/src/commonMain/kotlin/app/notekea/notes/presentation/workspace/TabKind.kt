package app.notekea.notes.presentation.workspace

/** What a tab shows: a note in the editor, another file's preview, a folder's grid, or a path that no longer exists. */
enum class TabKind {
    NOTE,
    FILE,
    FOLDER,
    MISSING
}
