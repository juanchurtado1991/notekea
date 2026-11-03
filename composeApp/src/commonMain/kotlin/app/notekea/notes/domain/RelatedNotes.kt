package app.notekea.notes.domain

/** The open note's [related] notes, closest first, and the notes it already [linked], in the order it links them. */
data class RelatedNotes(
    val related: List<RelatedNote>,
    val linked: List<RelativePath>
)
