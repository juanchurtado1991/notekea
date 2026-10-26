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

    /** Leading spaces and tabs, before any quote mark. */
    val indent: Int get() = prefix.takeWhile { StringPatterns.isIndent(char = it) }.length

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
        private val LINE = Regex(pattern = "^([ \\t]*(?:>[ \\t]?)*)([-*+] \\[[ xX]] |[-*+] |(\\d{1,9})([.)]) )?(.*)$")

        fun of(
            line: String
        ): ListLine {
            val match = LINE.find(input = line)!!
            return ListLine(
                prefix = match.groupValues[1],
                marker = match.groupValues[2],
                content = match.groupValues[5],
                delimiter = match.groupValues[4],
                number = match.groupValues[3].toIntOrNull()
            )
        }
    }
}
