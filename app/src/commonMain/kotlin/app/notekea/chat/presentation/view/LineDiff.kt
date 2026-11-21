package app.notekea.chat.presentation.view

/**
 * The words that differ between two lines: what is left between the words they start and end with alike, in each.
 * An empty range (a word only added, or only taken out) is the place where it was.
 */
internal object LineDiff {
    class Change(
        val old: IntRange,
        val new: IntRange
    )

    fun of(
        old: String,
        new: String
    ): Change {
        val oldWords = wordsOf(text = old)
        val newWords = wordsOf(text = new)

        val start = oldWords
            .zip(newWords)
            .takeWhile { (a, b) ->
                a.value == b.value
            }
            .size

        val most = minOf(oldWords.size, newWords.size) - start

        val end = oldWords
            .asReversed()
            .zip(newWords.asReversed())
            .take(n = most)
            .takeWhile { (a, b) ->
                a.value == b.value
            }
            .size

        return Change(
            old = rangeOf(
                text = old,
                words = oldWords,
                start = start,
                end = end
            ),
            new = rangeOf(
                text = new,
                words = newWords,
                start = start,
                end = end
            )
        )
    }

    private val WORD = Regex(pattern = "\\S+")

    private fun wordsOf(text: String): List<MatchResult> = WORD.findAll(input = text).toList()

    // The characters from the first changed word's start to the last one's end; empty where none changed.
    private fun rangeOf(
        text: String,
        words: List<MatchResult>,
        start: Int,
        end: Int
    ): IntRange {
        val changed = words.subList(
            fromIndex = start,
            toIndex = words.size - end
        )

        val at = words
            .getOrNull(index = start)
            ?.range
            ?.first ?: text.length

        if (changed.isEmpty()) return at until at

        return changed
            .first()
            .range
            .first..changed
            .last()
            .range
            .last
    }
}
