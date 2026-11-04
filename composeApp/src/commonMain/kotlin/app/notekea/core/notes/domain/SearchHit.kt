package app.notekea.core.notes.domain

/**
 * A chunk found by a search (`ROADMAP.md` 2.3): its [note], its [index] among the note's chunks, its [headingPath] and
 * its [range] in the note's body, and its [score] (higher is closer; only the order means something).
 */
data class SearchHit(
    val note: RelativePath,
    val index: Int,
    val headingPath: List<String>,
    val range: IntRange,
    val score: Float
)
