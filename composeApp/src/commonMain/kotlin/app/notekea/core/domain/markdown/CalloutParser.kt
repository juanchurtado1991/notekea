package app.notekea.core.domain.markdown

/**
 * Obsidian callouts (`ROADMAP.md` 1.7): a quote whose first line is `[!type]`, optionally followed by `-` or `+`
 * (foldable, shown closed or open) and a title. [header] reads that line ([ReadingMarkdown] makes it a paragraph of
 * its own).
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

    companion object {
        private const val COLLAPSED_MARK = "-"
        private const val EXPANDED_MARK = "+"
        private const val FOLD_GROUP = 2
        private const val TITLE_GROUP = 3
        private const val TYPE_GROUP = 1
        private val HEADER = Regex(pattern = "\\[!([^\\]\\s]+)]([+-]?)(?:\\s+(.*))?")
    }
}
