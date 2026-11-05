package app.notekea.shell.presentation

/** What a tab shows: a note in the editor, another file's preview, a folder's grid, or a path that no longer exists. */
enum class TabKind {
    NOTE,
    FILE,
    FOLDER,
    GRAPH,
    MISSING
}
