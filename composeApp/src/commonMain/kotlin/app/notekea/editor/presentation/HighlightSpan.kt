package app.notekea.editor.presentation

/** The characters from [start] (inclusive) to [end] (exclusive) of a note's body are a [kind]. */
data class HighlightSpan(
    val kind: HighlightKind,
    val start: Int,
    val end: Int
)
