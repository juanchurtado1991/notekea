package app.notekea.notes.domain

import app.notekea.core.domain.markdown.MarkdownSyntax
import app.notekea.core.domain.StringPatterns
/**
 * A line read as a list item or a quote line: [prefix] is its indentation and quote marks (`> `), [marker] its list
 * marker (`- `, `- [ ] `, `1. `; empty for a quote line or plain text) and [content] the rest. [number] and
 * [delimiter] belong to a numbered marker.
 */
data class ListLine(
    val prefix: String,
    val marker: String,
    val content: String,
    val delimiter: String,
    val number: Int?
) {
    val isItem: Boolean get() = marker.isNotEmpty()

    val isQuote: Boolean get() = MarkdownSyntax.QUOTE_MARK in prefix

    /** The part of [prefix] up to the last quote mark and its space; empty outside a quote. */
    val quote: String
        get() {
            val end = prefix.lastIndexOf(char = MarkdownSyntax.QUOTE_MARK) + 1
            if (end == 0) return StringPatterns.EMPTY
            val hasSpace = prefix.getOrNull(index = end)?.let { StringPatterns.isIndent(char = it) } == true
            return prefix.substring(startIndex = 0, endIndex = if (hasSpace) end + 1 else end)
        }

    /** The spaces and tabs before the marker, after any quote marks: where a list's levels are told apart. */
    val indent: Int get() = prefix.length - quote.length

    /** Where the item's text starts for Markdown, so a child indented this far nests under it: a task's box is text. */
    val textOffset: Int get() = if (marker.contains(
            other = MarkdownSyntax.TASK_BOX_OPEN
        )
    ) {
        MarkdownSyntax.BULLET.length
    } else {
        marker.length
    }

    /** The marker of the next item of the same list: the next number, or an unchecked task. */
    fun nextMarker(): String = when {
        number != null -> (number + 1).toString() + delimiter + StringPatterns.SPACE

        marker.contains(other = MarkdownSyntax.TASK_BOX_OPEN) -> marker.first() + StringPatterns.SPACE +
            MarkdownSyntax.TASK_BOX

        else -> marker
    }

    companion object {
        // `[\s\S]` and `\z`: a line can hold what `.` and `$` take for a line end (U+2028, U+2029, U+0085, a lone `\r`).
        private val LINE =
            Regex(pattern = "^([ \\t]*(?:>[ \\t]?)*[ \\t]*)([-*+] \\[[ xX]] |[-*+] |(\\d{1,9})([.)]) )?([\\s\\S]*)\\z")

        fun of(
            line: String
        ): ListLine {
            val groups = LINE.find(input = line)?.groupValues.orEmpty()
            return ListLine(
                prefix = groups.getOrElse(index = 1) { StringPatterns.EMPTY },
                marker = groups.getOrElse(index = 2) { StringPatterns.EMPTY },
                content = groups.getOrElse(index = 5) { line },
                delimiter = groups.getOrElse(index = 4) { StringPatterns.EMPTY },
                number = groups.getOrNull(index = 3)?.toIntOrNull()
            )
        }
    }
}
