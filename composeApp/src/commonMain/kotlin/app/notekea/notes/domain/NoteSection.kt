package app.notekea.notes.domain

/**
 * A part of a note's body under one heading, down to the next heading of any level, or the text before the first
 * heading: its [headingPath] (the titles above it and its own; empty before the first heading) and its [range] in the
 * body, its heading line included.
 */
data class NoteSection(
    val headingPath: List<String>,
    val range: IntRange
)
