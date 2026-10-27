package app.notekea.notes.domain

import kotlin.time.Instant

/** When a note's file was created and last modified; either is null where the file system does not say. */
data class NoteTimes(
    val createdAt: Instant?,
    val modifiedAt: Instant?
)
