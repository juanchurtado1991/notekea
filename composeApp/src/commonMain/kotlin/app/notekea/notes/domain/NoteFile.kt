package app.notekea.notes.domain

import kotlin.time.Instant

/**
 * A file of the vault as the file system reports it, a note or another file (an attachment); [size] and [modifiedAt]
 * tell whether a note's metadata is still fresh.
 * Either is null when the file system cannot tell, or the size when the note could not be read; the note is then always
 * read again.
 */
data class NoteFile(
    val path: RelativePath,
    val size: Long?,
    val modifiedAt: Instant?
)
