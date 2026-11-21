package app.notekea.core.notes.domain.index

import app.notekea.core.domain.markdown.FenceTracker

/**
 * What a result quotes of a note: the passage found ([text]) with the sentence that shares the
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
         * [passage] without its heading lines, each other line made [plain], and its sentence that [scores] best (by
         * default, how many of [words] it holds once [normalized]) as the highlight; the first when none scores.
         */
        fun of(
            passage: String,
            words: Set<String>,
            normalized: (String) -> String = String::lowercase,
            plain: (String) -> String = { it },
            scores: (String) -> Int = {
                sharedWords(
                    sentence = normalized(it),
                    words = words
                )
            }
        ): PassageSnippet {
            val text = withoutHeadings(lines = passage.lines())
                .map(transform = plain)
                .filter { it.isNotBlank() }
                .joinToString(separator = " ")
                .trim()

            val sentences = SENTENCE
                .findAll(input = text)
                .map { it.range }
                .toList()
                .ifEmpty { listOf(text.indices) }

            val best = sentences.maxBy { range -> scores(text.substring(range = range)) }

            return PassageSnippet(
                text = text,
                highlight = best,
                keywords = keywordsIn(text, best, words, normalized)
            )
        }

        /** [lines] without the headings among them; a `# comment` in a code block is no heading (bug hunt S8). */
        private fun withoutHeadings(lines: List<String>): List<String> {
            val fences = FenceTracker()

            return lines.filterNot {
                isHeading(
                    line = it,
                    fences = fences
                )
            }
        }

        // Every line goes through the fences, in order, so they know where code starts and ends.
        private fun isHeading(
            line: String,
            fences: FenceTracker
        ): Boolean {
            val isCode = fences.read(text = line)

            return !isCode && HEADING.matches(input = line)
        }

        private fun keywordsIn(
            text: String,
            sentence: IntRange,
            words: Set<String>,
            normalized: (String) -> String
        ): List<IntRange> {
            val shared = WORD
                .findAll(input = text.substring(range = sentence))
                .filter {
                    normalized(it.value) in words
                }
                .map {
                    (it.range.first + sentence.first)..(it.range.last + sentence.first) to
                        normalized(it.value)
                }
                .toList()

            val longest = shared
                .map { it.second }
                .distinct()
                .sortedByDescending { it.length }
                .take(n = MAX_KEYWORDS)

            return shared.filter {
                it.second in longest
            }.map { it.first }
        }

        private fun sharedWords(
            sentence: String,
            words: Set<String>
        ): Int = WORD.findAll(input = sentence).count {
            it.value in words
        }

        private const val MAX_KEYWORDS = 3
        private val HEADING = Regex(pattern = "^#{1,6}\\s.*$")

        // A sentence ends at its marks followed by a space or the end: a dot inside "3.500" or "v2.1" cut it.
        private val SENTENCE = Regex(pattern = ".+?(?:[.!?]+(?=\\s|$)|$)")
        private val WORD = Regex(pattern = "\\p{L}{3,}")
    }
}
