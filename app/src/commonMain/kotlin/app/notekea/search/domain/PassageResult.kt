package app.notekea.search.domain

import app.notekea.core.notes.domain.NoteEntry

/**
 * A note found by meaning or keywords (`ROADMAP.md` 2.6): its best line of the body ([range]), the closest [heading]
 * above it, and the [snippet] that shows why it was found, as plain text, with the searched words at [ranges].
 */
data class PassageResult(
    val note: NoteEntry,
    val snippet: String,
    val ranges: List<IntRange>,
    val range: IntRange,
    val heading: String?
)
