package app.notekea.notes.domain

/** A note whose links change: its text as it was read, and the same text with [linkCount] links rewritten. */
data class NoteRewrite(
    val path: RelativePath,
    val original: String,
    val updated: String,
    val linkCount: Int
)
