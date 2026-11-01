package app.notekea.notes.domain

/**
 * A piece of a note small enough to embed and to cite (`ROADMAP.md` 2.1): the [note] it comes from, its [index] among
 * the note's chunks, the [headingPath] of titles above it (`Arepas › Dough`), and its [range] in the note's body, the
 * text after the frontmatter with `\n` line breaks as the editor shows it ([NoteDocument]).
 * [text] is exactly that part of the body, so a citation can be checked word for word and opened where it is;
 * [tokens] is its size as [TokenEstimate] counts it.
 */
data class NoteChunk(
    val note: RelativePath,
    val index: Int,
    val headingPath: List<String>,
    val range: IntRange,
    val text: String,
    val tokens: Int
)
