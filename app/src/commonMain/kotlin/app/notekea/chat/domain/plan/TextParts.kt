package app.notekea.chat.domain.plan

/**
 * A pasted text in parts the model can read at once, cut between paragraphs. A paragraph longer than a part (a text
 * pasted with single line breaks) is cut by its lines, then by length: all of it past the first part was dropped
 * before, and nobody was told (code review, 2026-11-13).
 */
internal object TextParts {
    const val PART_CHARS = 5_000
    private val PARAGRAPH = Regex(pattern = "\\n\\s*\\n")

    fun of(
        text: String,
        partChars: Int = PART_CHARS
    ): List<String> {
        val paragraphs = text.split(PARAGRAPH).flatMap { paragraph ->
            if (paragraph.length <= partChars) {
                listOf(paragraph)
            } else {
                paragraph.lines().flatMap { it.chunked(size = partChars) }
            }
        }

        val parts = mutableListOf<StringBuilder>()

        paragraphs.forEach { paragraph ->
            val last = parts.lastOrNull()
            val fits = last != null && last.length + paragraph.length <= partChars

            if (fits) {
                last.append("\n\n").append(paragraph)
            } else {
                parts += StringBuilder(paragraph)
            }
        }

        return parts.map { it.toString() }.filter { it.isNotBlank() }
    }
}
