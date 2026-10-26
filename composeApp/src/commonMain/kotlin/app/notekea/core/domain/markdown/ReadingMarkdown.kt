package app.notekea.core.domain.markdown

import app.notekea.core.domain.StringPatterns
import app.notekea.core.domain.StringPatterns.NEW_LINE_CHAR
import app.notekea.core.domain.StringPatterns.NEW_LINE

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
        return markdown.split(NEW_LINE_CHAR).joinToString(separator = NEW_LINE) { line ->
            val marker = MarkdownSyntax.FENCE_LINE.find(input = line)?.groupValues?.get(index = 1)?.first()
            val calloutHeader = QUOTED_CALLOUT_HEADER.matchEntire(input = line)
            when {
                fence != null -> line.also { if (marker == fence) fence = null }
                marker != null -> line.also { fence = marker }
                calloutHeader != null -> line + NEW_LINE + calloutHeader.groupValues[1].trimEnd()
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
        val marks = Regex(pattern = QUOTE_MARKS_UP_TO + depth + QUOTE_MARKS_END)
        return code.split(NEW_LINE_CHAR).joinToString(separator = NEW_LINE) {
            marks.replace(input = it, replacement = StringPatterns.EMPTY)
        }
    }

    companion object {
        private const val CHECKED_TASK = "[x]"

        /** Up to a number of quote marks at the start of a line: `^(?:[ \t]*>[ ]?){1,` + depth + `}`. */
        private const val QUOTE_MARKS_UP_TO = "^(?:[ \\t]*>[ ]?){1,"
        private const val QUOTE_MARKS_END = "}"
        private val QUOTED_CALLOUT_HEADER = Regex(pattern = "(\\s*(?:>\\s?)+)\\[![^\\]\\s]+][+-]?.*")
        private val UPPERCASE_TASK = Regex(pattern = "^(\\s*(?:>\\s*)*(?:[-*+]|\\d+[.)])\\s+)\\[X](?=\\s|$)")
    }
}
