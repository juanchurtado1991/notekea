package app.notekea.documents.presentation

/**
 * A block of a document's text as the viewer shows it: a paragraph, a heading of [level] (1 to 6; 0 for none) or a
 * table row, and the [range] of the text it covers, so a cited passage is marked where it is.
 */
data class DocumentBlock(
    val text: String,
    val range: IntRange,
    val level: Int = 0,
    val isTableRow: Boolean = false
) {
    companion object {
        private val HEADING = Regex(pattern = "^(#{1,6}) (.*)$")
        private const val TABLE_RULE = "| ---"

        /** The blocks of [text]: one per paragraph or line of a list or table; a table's rule line left out. */
        fun of(text: String): List<DocumentBlock> {
            val blocks = mutableListOf<DocumentBlock>()
            var offset = 0

            text.split("\n").forEach { line ->
                val range = offset until offset + line.length
                offset += line.length + 1
                val isSkipped = line.isBlank() || line.startsWith(prefix = TABLE_RULE)
                if (isSkipped) return@forEach

                val heading = HEADING.matchEntire(input = line)

                blocks += when {
                    heading != null -> DocumentBlock(
                        text = heading.groupValues[2],
                        range = range,
                        level = heading.groupValues[1].length
                    )

                    line.startsWith(prefix = "|") -> DocumentBlock(
                        text = line
                            .trim('|')
                            .split(" | ")
                            .joinToString(separator = "   ") { it.trim() },
                        range = range,
                        isTableRow = true
                    )

                    else -> DocumentBlock(
                        text = line,
                        range = range
                    )
                }
            }

            return blocks
        }
    }
}
