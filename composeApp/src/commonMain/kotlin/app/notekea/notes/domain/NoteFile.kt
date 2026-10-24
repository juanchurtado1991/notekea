package app.notekea.notes.domain

import kotlin.time.Instant

/** A Markdown file as the file system reports it; [size] and [modifiedAt] tell whether its metadata is still fresh. */
data class NoteFile(
    val path: RelativePath,
    val size: Long,
    val modifiedAt: Instant
)
