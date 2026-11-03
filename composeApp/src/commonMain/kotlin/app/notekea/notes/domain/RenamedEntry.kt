package app.notekea.notes.domain

/**
 * Where a renamed entry now lives, and the link updates it calls for (`ROADMAP.md` 2.5): the notes that link to it,
 * each with its links rewritten, not yet written; empty when nothing links to it.
 */
data class RenamedEntry(
    val path: RelativePath,
    val rewrites: List<NoteRewrite>
)
