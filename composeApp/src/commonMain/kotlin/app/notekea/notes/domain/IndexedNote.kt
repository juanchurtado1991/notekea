package app.notekea.notes.domain

/**
 * A note as the search index holds it (`ROADMAP.md` 2.3): its [file] when it was indexed (size and date), the
 * [contentHash] of the text it was indexed from, and the index [version] it was indexed with.
 */
data class IndexedNote(
    val file: NoteFile,
    val contentHash: String,
    val version: String
)
