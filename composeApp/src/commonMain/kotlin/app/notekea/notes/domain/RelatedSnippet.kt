package app.notekea.notes.domain

/**
 * What a related note's card quotes (`ROADMAP.md` 2.4): its passage closest in meaning ([text]) with the sentence that
 * shares the most words with the open note [highlight]ed, so the person sees why it is related.
 */
data class RelatedSnippet(
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
        ): RelatedSnippet {
            val text = passage.lines().filterNot { HEADING.matches(input = it) }.joinToString(separator = " ").trim()
            val sentences = SENTENCE.findAll(input = text).map { it.range }.toList().ifEmpty { listOf(text.indices) }
            val best = sentences.maxBy { range -> sharedWords(sentence = text.substring(range = range), words = words) }
            return RelatedSnippet(text = text, highlight = best)
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
