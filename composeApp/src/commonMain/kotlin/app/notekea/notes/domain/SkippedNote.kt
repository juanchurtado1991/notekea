package app.notekea.notes.domain

import app.notekea.core.notes.domain.RelativePath

/** A note an indexing run left out, and [reason]; [cause] is what went wrong when nobody expected it. */
data class SkippedNote(
    val path: RelativePath,
    val reason: SkipReason,
    val cause: Throwable? = null
)
