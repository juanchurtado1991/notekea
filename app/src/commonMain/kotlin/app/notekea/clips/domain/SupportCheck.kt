package app.notekea.clips.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.llm.domain.LlmEngine

/**
 * Keeps of the model's notes only the sentences the article backs (4.4, as 3.5 checks answers): each sentence is
 * scored by the reranker against the article's passages, and one under [SUPPORT] for all of them is dropped; a
 * heading with nothing left under it goes too.
 */
class SupportCheck(private val llmEngine: LlmEngine) {
    suspend fun backed(
        notes: String,
        article: Article
    ): String {
        val passages = passagesOf(article = article)
        if (passages.isEmpty()) return ""

        val kept = notes.lines().mapNotNull { line ->
            keptOf(
                line = line,
                passages = passages
            )
        }

        return withoutEmptyHeadings(lines = kept).joinToString(separator = "\n").trim()
    }

    /** [line] with only its backed sentences; null when none is. */
    private suspend fun keptOf(
        line: String,
        passages: List<String>
    ): String? {
        val isHeadingOrBlank = line.isBlank() || line.trimStart().startsWith(prefix = "#")
        if (isHeadingOrBlank) return line

        val marker = MARKER
            .find(input = line)
            ?.value
            .orEmpty()

        val sentences = SENTENCE
            .findAll(input = line.removePrefix(prefix = marker))
            .map { it.value.trim() }
            .toList()

        val backed = sentences.filter { sentence ->
            val scores = llmEngine.rerank(
                query = sentence,
                texts = passages
            )

            ((scores as? Outcome.Success)?.value?.maxOrNull() ?: Float.NEGATIVE_INFINITY) >= SUPPORT
        }

        return if (backed.isEmpty()) null else marker + backed.joinToString(separator = " ")
    }

    private fun withoutEmptyHeadings(lines: List<String>): List<String> =
        lines.filterIndexed { index, line ->
            if (!line.trimStart().startsWith(prefix = "#")) return@filterIndexed true

            lines
                .drop(n = index + 1)
                .takeWhile {
                    !it.trimStart().startsWith(prefix = "#")
                }
                .any { it.isNotBlank() }
        }

    private fun passagesOf(article: Article): List<String> {
        val windows = mutableListOf<String>()
        val current = StringBuilder()

        article.paragraphs.forEach { paragraph ->
            val isFull = current.length + paragraph.length > PASSAGE_CHARS && current.isNotEmpty()

            if (isFull) {
                windows += current.toString()
                current.clear()
            }

            current.append(paragraph).append('\n')
        }

        if (current.isNotBlank()) windows += current.toString()

        return windows.take(n = MAX_PASSAGES)
    }

    companion object {
        /** The reranker's score a sentence needs against one passage to be backed by the article. */
        const val SUPPORT = -3f
        private const val PASSAGE_CHARS = 700
        private const val MAX_PASSAGES = 40
        private val MARKER = Regex(pattern = "^\\s*([-*+] |\\d+[.)] )")
        private val SENTENCE = Regex(pattern = "[^.!?]+[.!?]*")
    }
}
