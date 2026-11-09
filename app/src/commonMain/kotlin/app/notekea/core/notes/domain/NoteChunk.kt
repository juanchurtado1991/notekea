package app.notekea.core.notes.domain

/**
 * A piece of a note small enough to embed and to cite: the [note] it comes from, its [index] among the note's chunks,
 * the [headingPath] of titles above it and its [range] in the note's body ([NoteDocument]). [text] is exactly that part
 * of the body, so a citation can be checked word for word; [tokens] is its size as [TokenEstimate] counts it.
 */
data class NoteChunk(
    val note: RelativePath,
    val index: Int,
    val headingPath: List<String>,
    val range: IntRange,
    val text: String,
    val tokens: Int
)
