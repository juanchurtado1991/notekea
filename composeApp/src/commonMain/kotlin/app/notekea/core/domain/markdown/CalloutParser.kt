package app.notekea.core.domain.markdown

/**
 * Obsidian callouts (`ROADMAP.md` 1.7): a quote whose first line is `[!type]`, optionally followed by `-` or `+`
 * (foldable, shown closed or open) and a title. [header] reads that line. [separateHeaders] prepares a note for a
 * Markdown renderer: it adds an empty quoted line after every callout's first line, so the header is a paragraph of
 * its own instead of the start of the callout's first paragraph; nested callouts keep their depth, and fenced code is
 * left alone.
 */
class CalloutParser {
    fun header(
        line: String
    ): CalloutHeader? {
        val match = HEADER.matchEntire(input = line.trim()) ?: return null
        val type = match.groupValues[TYPE_GROUP]
        val fold = when (match.groupValues[FOLD_GROUP]) {
            COLLAPSED_MARK -> CalloutFold.COLLAPSED
            EXPANDED_MARK -> CalloutFold.EXPANDED
            else -> CalloutFold.NONE
        }
        val title = match.groupValues[TITLE_GROUP].trim().ifEmpty { type.replaceFirstChar { it.uppercaseChar() } }
        return CalloutHeader(kind = CalloutKind.of(type = type), title = title, fold = fold)
    }

    fun separateHeaders(
        markdown: String
    ): String {
        var fence: Char? = null
        return markdown.split('\n').joinToString(separator = "\n") { line ->
            val marker = FENCE.find(input = line)?.groupValues?.get(index = 1)?.first()
            val headerLine = QUOTED_HEADER.matchEntire(input = line)
            when {
                fence != null -> line.also { if (marker == fence) fence = null }
                marker != null -> line.also { fence = marker }
                headerLine != null -> line + "\n" + headerLine.groupValues[1].trimEnd()
                else -> line
            }
        }
    }

    companion object {
        private const val COLLAPSED_MARK = "-"
        private const val EXPANDED_MARK = "+"
        private const val FOLD_GROUP = 2
        private const val TITLE_GROUP = 3
        private const val TYPE_GROUP = 1
        private val FENCE = Regex(pattern = "^\\s{0,3}(`{3,}|~{3,})")
        private val HEADER = Regex(pattern = "\\[!([^\\]\\s]+)]([+-]?)(?:\\s+(.*))?")
        private val QUOTED_HEADER = Regex(pattern = "(\\s*(?:>\\s?)+)\\[![^\\]\\s]+][+-]?.*")
    }
}
