package app.notekea.chat.domain

/**
 * Deep answer's choice among drafts of one answer (3.6, `docs/spikes/2026-11-08-answer-quality.md`): the notes do not
 * answer when most drafts say [AnswerPrompt.NOT_FOUND]; otherwise the draft whose words the others share most wins.
 */
object DraftVote {
    private val WORD = Regex(pattern = "[\\p{L}\\p{N}]+")

    /** The winning draft of [drafts], or null when most of them found nothing. */
    fun winner(drafts: List<String>): String? {
        val answered = drafts.filterNot { isNotFound(draft = it) }
        val isDeclined = answered.size * 2 <= drafts.size
        if (isDeclined) return null

        val words = answered.map { wordsOf(text = it) }

        return answered
            .indices
            .maxBy { index ->
                words
                    .indices
                    .filter {
                        it != index
                    }
                    .sumOf { other ->
                        overlap(
                            first = words[index],
                            second = words[other]
                        )
                    }
            }
            .let {
                answered[it]
            }
    }

    /** Whether [draft] says the notes do not answer. */
    fun isNotFound(draft: String): Boolean {
        val text = draft.trim()

        return text.isEmpty() || AnswerPrompt.NOT_FOUND in text
    }

    private fun wordsOf(text: String): Set<String> = WORD
        .findAll(input = text.lowercase())
        .map { it.value }
        .toSet()

    /** The share of words two drafts have in common (Jaccard). */
    private fun overlap(
        first: Set<String>,
        second: Set<String>
    ): Double {
        val union = (first + second).size
        if (union == 0) return 0.0

        return (first intersect second).size.toDouble() / union
    }
}
