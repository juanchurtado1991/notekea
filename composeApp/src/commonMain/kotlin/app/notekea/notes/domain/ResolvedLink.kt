package app.notekea.notes.domain

/** The note a link points to, and whether its (Markdown) target was written relative to the linking note's folder. */
data class ResolvedLink(
    val target: RelativePath,
    val isRelativeToSource: Boolean
)
