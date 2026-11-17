package app.notekea.core.domain.markdown

/**
 * Follows the fenced code blocks of a text read line by line, as CommonMark does: three or more backticks or tildes
 * opening a line (also in a quote or under a list item) open one, and only a bare line of the same character, at least
 * as many, closes it. Every reader of Markdown goes through it, so they all agree on what is code.
 */
class FenceTracker {
    private var fence: String? = null
    private var fenceIndent = 0

    // Where the text of the list item read last starts: a fence indented up to it is the item's, any deeper is not.
    private var listContent = 0

    /** Whether a code block is open after the last line read. */
    val isOpen: Boolean get() = fence != null

    /**
     * Reads the next line, the part of [text] from [start] until [end], and tells whether it belongs to a code block,
     * its fence lines included. Most lines hold no fence character and are not matched against a fence.
     */
    fun read(
        text: CharSequence,
        start: Int = 0,
        end: Int = text.length
    ): Boolean {
        val open = fence

        // A fence inside a quote (`> ````) or indented under a list item is code too: its sample tasks and tags
        // counted as the note's own (bug hunt K5). The quote marks and the indentation are read before the fence.
        val quoteEnd = quoteEndOf(
            text = text,
            start = start,
            end = end
        )

        val contentStart = (quoteEnd until end).firstOrNull {
            !text[it].isWhitespace()
        } ?: end

        if (open == null) {
            followList(
                text = text,
                at = contentStart,
                end = end,
                indent = contentStart - quoteEnd
            )
        }

        val hasFence = (contentStart until end).any {
            text[it] in FENCE_CHARS
        }

        if (!hasFence) return open != null

        val line = text.subSequence(
            startIndex = contentStart,
            endIndex = end
        )

        val indent = contentStart - quoteEnd

        val mark = MarkdownSyntax.FENCE_LINE
            .find(input = line)
            ?.groupValues
            ?.get(index = 1)

        // Deeper than three spaces past where a list item's text starts, a fence line is indented code, not a fence.
        val isOpening = mark != null && indent <= listContent + MAX_CLOSING_INDENT

        if (open == null) {
            fence = mark?.takeIf { isOpening }
            fenceIndent = indent
        } else if (mark != null) {
            val isClosing = closes(
                line = line,
                mark = mark,
                open = open,
                indent = indent
            )

            if (isClosing) fence = null
        }

        return open != null || fence != null
    }

    // A list item's marker sets where its text starts; a line with less indent than that ends the item.
    private fun followList(
        text: CharSequence,
        at: Int,
        end: Int,
        indent: Int
    ) {
        if (at >= end) return

        val marker = LIST_MARKER.find(
            input = text.subSequence(
                startIndex = at,
                endIndex = end
            )
        )

        listContent = when {
            marker != null -> indent + marker.value.length
            indent < listContent -> 0
            else -> listContent
        }
    }

    /** Where the `>` marks that open the line end: after the last of them, or [start] when it has none. */
    private fun quoteEndOf(
        text: CharSequence,
        start: Int,
        end: Int
    ): Int {
        var quoteEnd = start
        var index = start

        while (index < end) {
            val char = text[index]

            when {
                char == MarkdownSyntax.QUOTE_MARK -> quoteEnd = index + 1
                !char.isWhitespace() -> break
            }

            index++
        }

        return quoteEnd
    }

    // A closing fence may be indented up to three spaces more than its opening one; deeper, it is the block's text.
    private fun closes(
        line: CharSequence,
        mark: String,
        open: String,
        indent: Int
    ): Boolean =
        mark.first() == open.first() &&
            mark.length >= open.length &&
            indent <= fenceIndent + MAX_CLOSING_INDENT &&
            line.trim().length == mark.length

    private companion object {
        const val FENCE_CHARS = "`~"
        const val MAX_CLOSING_INDENT = 3
        val LIST_MARKER = Regex(pattern = "^([-*+]|\\d{1,9}[.)])[ \\t]+")
    }
}
