package app.notekea.notes.domain

/** A line of a note as plain text, without its Markdown syntax, and the [ranges] of its highlights in that text. */
data class PlainLine(
    val text: String,
    val ranges: List<IntRange>
)
