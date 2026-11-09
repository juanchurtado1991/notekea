package app.notekea.core.domain.markdown

/**
 * Follows the fenced code blocks of a text read line by line, as CommonMark does: a line starting with three or more
 * backticks or tildes opens one, and only a line of the same character, at least as many and nothing else after them,
 * closes it. Every reader of Markdown goes through it, so they all agree on what is code.
 */
class FenceTracker {
    private var fence: String? = null

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

        val hasFence = (start until end).any {
            text[it] in FENCE_CHARS
        }

        val line = if (hasFence) {
            text.subSequence(
                startIndex = start,
                endIndex = end
            )
        } else {
            EMPTY_LINE
        }

        val mark = MarkdownSyntax.FENCE_LINE
            .find(input = line)
            ?.groupValues
            ?.get(index = 1)

        when {
            open == null -> fence = mark
            mark != null && closes(line = line, mark = mark, open = open) -> fence = null
        }

        return open != null || mark != null
    }

    private fun closes(
        line: CharSequence,
        mark: String,
        open: String
    ): Boolean =
        mark.first() == open.first() &&
            mark.length >= open.length &&
            line.trim().length == mark.length

    private companion object {
        const val EMPTY_LINE = ""
        const val FENCE_CHARS = "`~"
    }
}
