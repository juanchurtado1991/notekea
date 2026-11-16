package app.notekea.chat.domain.context

/**
 * A step's result cut into the parts [StepMemory] keeps: its first line (what was called, on what) and then its
 * paragraphs, joined up to [MOST_CHARS] and a longer one cut there. The same result always gives the same parts.
 */
object StepChunks {
    fun of(entry: String): List<String> {
        val lines = entry.trim().lines()
        val first = lines.firstOrNull().orEmpty()

        val paragraphs = lines
            .drop(n = 1)
            .joinToString(separator = "\n")
            .split(PARAGRAPH)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .flatMap { it.chunked(size = MOST_CHARS) }

        val joined = paragraphs
            .fold(initial = emptyList<String>()) { parts, paragraph ->
                val last = parts.lastOrNull()
                val isJoined = last != null && last.length + paragraph.length < MOST_CHARS

                if (isJoined) parts.dropLast(n = 1) + "$last\n\n$paragraph" else parts + paragraph
            }

        return listOf(first) + joined
    }

    /** The result's first line, short: what the step was ("fetch https://…", "read Projects/Irrigation"). */
    fun headingOf(entry: String): String = entry
        .trim()
        .lineSequence()
        .firstOrNull()
        .orEmpty()
        .take(n = MOST_HEADING)

    private val PARAGRAPH = Regex(pattern = "\\n\\s*\\n")
    private const val MOST_CHARS = 700
    private const val MOST_HEADING = 120
}
