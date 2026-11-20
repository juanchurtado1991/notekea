package app.notekea.chat.domain.plan

/**
 * A line of a note as `change_line` rewrites it (4.2): the marks a line's text is written after (its indent, quote
 * marks, a heading's, a list item's or a task's) are kept when the new line leaves them out, as a model writes the
 * words it changes and not the "- " before them.
 */
object LineEdit {
    private val MARKS =
        Regex(pattern = "^\\s*(?:>\\s*)*(?:#{1,6}\\s+|(?:[-*+]|\\d+[.)])\\s+(?:\\[.]\\s+)?)?")
    private val INDENT = Regex(pattern = "^\\s*")

    /** The marks [line] is written after, its indent first; empty when it has none. */
    fun marksOf(line: String): String = MARKS
        .find(input = line)
        ?.value
        .orEmpty()

    /** [line]'s text without the marks it is written after. */
    fun textOf(line: String): String = line
        .removePrefix(prefix = marksOf(line = line))
        .trim()

    /** [new] as written in place of [old]: after [old]'s marks when it has none of its own, else at [old]'s indent. */
    fun written(
        old: String,
        new: String
    ): String {
        val trimmed = new.trim()
        val isBare = marksOf(line = trimmed).isBlank()

        val indent = INDENT
            .find(input = old)
            ?.value
            .orEmpty()

        return if (isBare) marksOf(line = old) + trimmed else indent + trimmed
    }

    /**
     * [text] with the line [old] means ([LineMatch], the frontmatter left out) written as [new]; null when no line,
     * or several alike, read like [old]. Each other line, and how the note ends its lines, stays as it was.
     */
    fun changed(
        text: String,
        old: String,
        new: String
    ): String? {
        val lines = text.split('\n')
        val start = bodyStartOf(lines = lines)

        val match = LineMatch.of(
            lines = lines
                .drop(n = start)
                .map { it.removeSuffix(suffix = "\r") },
            wanted = old
        ) as? LineMatch.One ?: return null

        val at = start + match.at
        val ending = if (lines[at].endsWith(suffix = "\r")) "\r" else ""

        val line = written(
            old = match.line,
            new = new
        ) + ending

        return lines
            .toMutableList()
            .apply {
                set(
                    index = at,
                    element = line
                )
            }
            .joinToString(separator = "\n")
    }

    // The index of the first line after the frontmatter: 0 when the note has none, or it is never closed.
    private fun bodyStartOf(lines: List<String>): Int {
        val isOpened = lines.firstOrNull()?.trimEnd() == FENCE
        if (!isOpened) return 0

        val closing = lines
            .drop(n = 1)
            .indexOfFirst {
                it.trimEnd() == FENCE
            }

        return if (closing < 0) 0 else closing + 2
    }

    private const val FENCE = "---"
}
