package app.notekea.notes.domain

/**
 * How well a search matched a text: a higher [score] is a better match, and [ranges] are the matched characters of the
 * text (merged into runs), for highlighting. An empty search matches everything with a score of zero and no ranges.
 */
data class FuzzyMatch(
    val score: Int,
    val ranges: List<IntRange>
) {
    companion object {
        val EVERYTHING = FuzzyMatch(score = 0, ranges = emptyList())
    }
}
