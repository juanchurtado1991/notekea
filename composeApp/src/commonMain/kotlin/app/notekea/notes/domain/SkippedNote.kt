package app.notekea.notes.domain

/** A note an indexing run left out, and [reason]. */
data class SkippedNote(
    val path: RelativePath,
    val reason: SkipReason
)
