package app.notekea.core.notes.domain.index

/**
 * Merges rankings of the same items into one, by reciprocal rank fusion: an item scores 1 / ([CONSTANT] + its rank) in
 * each ranking it is in, and the highest total comes first. Only the ranks count, so closeness in meaning and keyword
 * matches weigh the same; ties keep the order in which items first appear.
 */
object RankFusion {
    fun <K> merge(rankings: List<List<K>>): List<Pair<K, Float>> {
        val scores = LinkedHashMap<K, Float>()

        rankings.forEach { ranking ->
            ranking.forEachIndexed { rank, item ->
                scores[item] =
                    (scores[item] ?: 0f) + 1f / (CONSTANT + rank + 1)
            }
        }

        return scores
            .entries
            .sortedByDescending { it.value }
            .map {
                it.key to it.value
            }
    }

    // The usual constant of reciprocal rank fusion: it keeps the first few ranks from outweighing the rest.
    private const val CONSTANT = 60
}
