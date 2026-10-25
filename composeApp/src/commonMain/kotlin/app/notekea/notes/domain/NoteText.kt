package app.notekea.notes.domain

/** A note's [body] (the text after its frontmatter, with `\n` line breaks) and the same text [folded] for searching. */
data class NoteText(
    val body: String,
    val folded: String
)
