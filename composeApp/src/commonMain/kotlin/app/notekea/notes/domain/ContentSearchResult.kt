package app.notekea.notes.domain

/**
 * A note found by content search. [snippet] is the first line of the body that holds a searched word or `#tag`
 * (shortened around the match when long, with `…`), and [ranges] its matched characters; both are null and empty when
 * the note matched only by a tag of its frontmatter. [matchCount] is how many times the search appears in the body.
 */
data class ContentSearchResult(
    val note: NoteEntry,
    val ranges: List<IntRange>,
    val matchCount: Int,
    val snippet: String?
)
