package app.notekea.notes.domain

/** What a quick switcher result matched: its name, an alias (or the frontmatter title), its path, or nothing yet. */
enum class NoteMatchField {
    NAME,
    ALIAS,
    PATH,
    RECENT
}
