package app.notekea.cards.eval

/**
 * The sentences of a note a study card could be made from, as the cards' judge reads them: its lines of prose, list
 * items and table rows, cut at the end of each sentence; the properties, headings, code, the cards' own lines and a
 * link alone left out, and nothing shorter than [MIN_CHARS].
 */
internal object NoteSentences {
    private const val MIN_CHARS = 12
    private const val FENCE = "```"
    private const val PROPERTIES = "---"
    private val END = Regex(pattern = "(?<=[.!?])\\s+(?=\\p{Lu}|\\d)")
    private val MARK = Regex(pattern = "^([-*+]\\s+(\\[.]\\s+)?|\\d+[.)]\\s+|>\\s*)")
    private val LINK_ONLY = Regex(pattern = "^\\[\\[[^]]+]]$")
    private val TABLE_RULE = Regex(pattern = "^\\|?\\s*:?-{3,}")

    fun of(text: String): List<String> = bodyOf(text = text)
        .lines()
        .map { it.trim() }
        .filterNot(::isSkipped)
        .withoutCode()
        .flatMap { line ->
            MARK
                .replace(
                    input = line,
                    replacement = ""
                )
                .trim()
                .trim('|')
                .replace(
                    oldValue = "|",
                    newValue = ";"
                )
                .split(END)
                .map { it.trim() }
        }
        .filter {
            it.length >= MIN_CHARS && !LINK_ONLY.matches(input = it)
        }

    // The note without its properties, the block between two "---" lines at its top.
    private fun bodyOf(text: String): String {
        if (!text.startsWith(prefix = PROPERTIES)) return text

        val end = text.indexOf(
            string = "\n$PROPERTIES",
            startIndex = PROPERTIES.length
        )

        return if (end < 0) text else text.substring(startIndex = end + PROPERTIES.length + 1)
    }

    private fun isSkipped(line: String): Boolean =
        line.startsWith(prefix = "#") ||
            "::" in line ||
            line.startsWith(prefix = "<!--") ||
            TABLE_RULE.containsMatchIn(input = line)

    // The lines outside code blocks, the fences themselves left out.
    private fun List<String>.withoutCode(): List<String> {
        var isCode = false

        return filter { line ->
            val isFence = line.startsWith(prefix = FENCE)
            if (isFence) isCode = !isCode

            !isFence && !isCode
        }
    }
}
