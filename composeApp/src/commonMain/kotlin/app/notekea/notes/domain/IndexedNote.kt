package app.notekea.notes.domain

import app.notekea.core.notes.domain.NoteFile

/**
 * A note as the search index holds it (`ROADMAP.md` 2.3): its [file] when it was indexed (size and date), the
 * [contentHash] of the text it was indexed from, and the index [version] it was indexed with. A note left out on
 * purpose is held too, with its [skipReason] and nothing in the index, so it is not read again until it changes.
 */
data class IndexedNote(
    val file: NoteFile,
    val contentHash: String,
    val version: String,
    val skipReason: SkipReason? = null
)
