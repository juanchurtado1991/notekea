package app.notekea.chat.domain.tool.lookup

import app.notekea.chat.domain.tool.reading.TextOutline

/**
 * What a long note holds after the part a read shows: its headings, and the paragraphs that write the task's
 * words, so an answer is not given from the first part alone (the loop's eighth run said "the agreement does not
 * say what happens to the data" from the first 2,500 of 8,685 characters, and it says it further down).
 */
internal object LongNote {
    fun restOf(
        text: String,
        task: String,
        shown: Int
    ): String {
        val named = TextOutline.of(text = text).filter {
            it.level > 0
        }

        // The part the first characters end in, by its innermost heading, and every part that starts after them.
        val cut = named.lastOrNull {
            it.start < shown && it.end > shown
        }

        val parts = listOfNotNull(cut) + named.filter {
            it.start >= shown
        }

        val headings = parts
            .take(n = HEADINGS)
            .joinToString { it.heading }
            .takeIf { it.isNotEmpty() }
            ?.let { "\nThe rest is under these headings: $it. Read one whole with section." }
            .orEmpty()

        val written = writingOf(
            rest = text.drop(n = shown),
            task = task
        )

        val found = written
            .takeIf { it.isNotEmpty() }
            ?.joinToString(
                separator = "\n",
                prefix = "\nFrom the rest, the paragraphs that write the task's words:\n"
            ) { "- $it" }
            .orEmpty()

        return "$headings$found\nWhat the note says is in all of it, not only in its first part."
    }

    // The paragraphs that write the task's rarest words, in the note's order: a word every paragraph writes
    // ("agreement" in a contract) tells none apart, so each word counts for less the more paragraphs write it.
    private fun writingOf(
        rest: String,
        task: String
    ): List<String> {
        val words = NOT_WORD
            .split(input = task.lowercase())
            .filter {
                it.length >= MIN_WORD
            }
            .map { it.removeSuffix(suffix = "s") }
            .distinct()

        val paragraphs = rest
            .split(BLANK_LINE)
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val lowered = paragraphs.map { it.lowercase() }

        val weights = words.associateWith { word ->
            lowered
                .count {
                    word in it
                }
                .takeIf {
                    it > 0
                }
                ?.let {
                    1.0 / it
                }
                ?: 0.0
        }

        val best = lowered
            .mapIndexed { index, paragraph ->
                index to words.sumOf {
                    if (it in paragraph) weights.getValue(key = it) else 0.0
                }
            }
            .filter {
                it.second > 0.0
            }
            .sortedByDescending { it.second }
            .take(n = PARAGRAPHS)
            .map { it.first }
            .sorted()

        var left = CHARS

        return best.mapNotNull { index ->
            val paragraph = paragraphs[index].take(n = PARAGRAPH_CHARS)
            left -= paragraph.length

            paragraph.takeIf {
                left >= 0
            }
        }
    }

    private const val HEADINGS = 40
    private const val PARAGRAPHS = 5
    private const val PARAGRAPH_CHARS = 700
    private const val CHARS = 2_000
    private const val MIN_WORD = 4
    private val NOT_WORD = Regex(pattern = "[^\\p{L}\\p{N}]+")
    private val BLANK_LINE = Regex(pattern = "\\n\\s*\\n")
}
