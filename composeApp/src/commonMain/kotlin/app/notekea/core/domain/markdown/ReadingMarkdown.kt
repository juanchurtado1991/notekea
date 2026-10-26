package app.notekea.core.domain.markdown

/**
 * A note's Markdown prepared for a renderer that knows less than Obsidian (Read mode). Every callout's first line
 * (`> [!type] Title`, nested ones included) gets an empty quoted line after it, so the header is a paragraph of its own
 * instead of the start of the callout's first paragraph ([CalloutParser]); and a task checked with a capital `[X]` is
 * written `[x]`, which is the only form the renderer checks. Fenced code is left as it is; [unquoted] cleans the code
 * of a block inside quotes.
 */
class ReadingMarkdown {
    fun prepare(
        markdown: String
    ): String {
        var fence: Char? = null
        return markdown.split('\n').joinToString(separator = "\n") { line ->
            val marker = FENCE.find(input = line)?.groupValues?.get(index = 1)?.first()
            val calloutHeader = QUOTED_CALLOUT_HEADER.matchEntire(input = line)
            when {
                fence != null -> line.also { if (marker == fence) fence = null }
                marker != null -> line.also { fence = marker }
                calloutHeader != null -> line + "\n" + calloutHeader.groupValues[1].trimEnd()
                else -> UPPERCASE_TASK.replace(input = line) { it.groupValues[1] + CHECKED_TASK }
            }
        }
    }

    /**
     * The code of a code block that sits [depth] quotes or callouts deep, without the `>` marks the renderer leaves at
     * the start of its lines.
     */
    fun unquoted(
        code: String,
        depth: Int
    ): String {
        if (depth == 0) return code
        val marks = Regex(pattern = "^(?:[ \\t]*>[ ]?){1,$depth}")
        return code.split('\n').joinToString(separator = "\n") { marks.replace(input = it, replacement = "") }
    }

    companion object {
        private const val CHECKED_TASK = "[x]"
        private val FENCE = Regex(pattern = "^\\s{0,3}(`{3,}|~{3,})")
        private val QUOTED_CALLOUT_HEADER = Regex(pattern = "(\\s*(?:>\\s?)+)\\[![^\\]\\s]+][+-]?.*")
        private val UPPERCASE_TASK = Regex(pattern = "^(\\s*(?:>\\s*)*(?:[-*+]|\\d+[.)])\\s+)\\[X](?=\\s|$)")
    }
}
