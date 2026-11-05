package app.notekea.core.notes.domain

/**
 * What a result quotes of a note (`ROADMAP.md` 2.4, 2.6): the passage found ([text]) with the sentence that shares the
 * most words with what was looked for as [highlight], and in it the [keywords] that say why it was found: the few
 * longest words it shares, so a row marks those and not whole lines.
 */
data class PassageSnippet(
    val text: String,
    val highlight: IntRange,
    val keywords: List<IntRange> = emptyList()
) {
    companion object {
        /**
         * [passage] without its heading lines, and its sentence sharing most of [words], which are [normalized], lower
         * case unless told otherwise, as the highlight; the first sentence when none shares any.
         */
        fun of(
            passage: String,
            words: Set<String>,
            normalized: (String) -> String = String::lowercase
        ): PassageSnippet {
            val text = passage.lines().filterNot { HEADING.matches(input = it) }.joinToString(separator = " ").trim()
            val sentences = SENTENCE.findAll(input = text).map { it.range }.toList().ifEmpty { listOf(text.indices) }
            val best = sentences.maxBy { range ->
                sharedWords(sentence = normalized(text.substring(range = range)), words = words)
            }
            return PassageSnippet(text = text, highlight = best, keywords = keywordsIn(text, best, words, normalized))
        }

        private fun keywordsIn(
            text: String,
            sentence: IntRange,
            words: Set<String>,
            normalized: (String) -> String
        ): List<IntRange> {
            val shared = WORD.findAll(input = text.substring(range = sentence))
                .filter { normalized(it.value) in words }
                .map { (it.range.first + sentence.first)..(it.range.last + sentence.first) to normalized(it.value) }
                .toList()
            val longest = shared.map { it.second }.distinct().sortedByDescending { it.length }.take(n = MAX_KEYWORDS)
            return shared.filter { it.second in longest }.map { it.first }
        }

        private fun sharedWords(
            sentence: String,
            words: Set<String>
        ): Int = WORD.findAll(input = sentence).count { it.value in words }

        private const val MAX_KEYWORDS = 3
        private val HEADING = Regex(pattern = "^#{1,6}\\s.*$")
        private val SENTENCE = Regex(pattern = "[^.!?]+[.!?]?")
        private val WORD = Regex(pattern = "\\p{L}{3,}")
    }
}
