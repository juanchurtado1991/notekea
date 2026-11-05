package app.notekea.core.notes.domain

/**
 * What a result quotes of a note (`ROADMAP.md` 2.4, 2.6): the passage found ([text]) with the sentence that shares the
 * most words with what was looked for [highlight]ed, so the person sees why it was found.
 */
data class PassageSnippet(
    val text: String,
    val highlight: IntRange
) {
    companion object {
        /**
         * [passage] without its heading lines, and its sentence sharing most of [words] (lower case) as the
         * highlight; the first sentence when none shares any.
         */
        fun of(
            passage: String,
            words: Set<String>
        ): PassageSnippet {
            val text = passage.lines().filterNot { HEADING.matches(input = it) }.joinToString(separator = " ").trim()
            val sentences = SENTENCE.findAll(input = text).map { it.range }.toList().ifEmpty { listOf(text.indices) }
            val best = sentences.maxBy { range -> sharedWords(sentence = text.substring(range = range), words = words) }
            return PassageSnippet(text = text, highlight = best)
        }

        private fun sharedWords(
            sentence: String,
            words: Set<String>
        ): Int = WORD.findAll(input = sentence.lowercase()).count { it.value in words }

        private val HEADING = Regex(pattern = "^#{1,6}\\s.*$")
        private val SENTENCE = Regex(pattern = "[^.!?]+[.!?]?")
        private val WORD = Regex(pattern = "\\p{L}{3,}")
    }
}
