package app.notekea.sync.presentation

import app.notekea.core.notes.domain.RelativePath

/** A conflict open for review: the [note], our version ([mine], from its conflict copy) and the remote's ([theirs]). */
data class ConflictReview(
    val note: RelativePath,
    val mine: String,
    val theirs: String
)
