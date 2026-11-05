package app.notekea.core.notes.domain

import app.notekea.core.notes.domain.RelativePath

/**
 * The open note's [related] notes, closest first, and the notes it already [linked], in the order it links them;
 * [isTooLong] when the model could read none of it, so nothing can be related to it.
 */
data class RelatedNotes(
    val related: List<RelatedNote>,
    val linked: List<RelativePath>,
    val isTooLong: Boolean = false
)
