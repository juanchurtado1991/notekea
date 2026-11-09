package app.notekea.search.domain

import app.notekea.core.notes.domain.NoteEntry

/**
 * A note found by content search. [snippet] is the first line of the body that holds a searched word or tag (shortened
 * around the match when long) and [ranges] its matched characters; a note matched only by a frontmatter tag has
 * neither. [line] is where that line is in the body; [matchCount] how many times the search appears.
 */
data class ContentSearchResult(
    val note: NoteEntry,
    val ranges: List<IntRange>,
    val matchCount: Int,
    val snippet: String?,
    val line: IntRange? = null
)
