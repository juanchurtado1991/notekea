package app.notekea.notes.domain

import app.notekea.core.domain.StringPatterns
import app.notekea.core.domain.markdown.MarkdownSyntax

/**
 * The headings of a note's body in text order (`ROADMAP.md` 1.9): every line that starts with one to six `#` and a
 * space, as the editor highlights them, outside fenced code blocks. Their text is shown as it reads
 * ([MarkdownLineStripper]); a closing run of `#` is dropped, and a heading with no text is left out.
 */
class OutlineFinder {
    private val stripper = MarkdownLineStripper()

    fun headings(
        text: String
    ): List<OutlineHeading> {
        val headings = mutableListOf<OutlineHeading>()
        val openLevels = ArrayDeque<Int>()
        var fence: String? = null
        var lineStart = 0
        while (lineStart <= text.length) {
            val newline = text.indexOf(char = StringPatterns.NEW_LINE_CHAR, startIndex = lineStart)
            val lineEnd = if (newline < 0) text.length else newline
            val line = text.substring(startIndex = lineStart, endIndex = lineEnd).removeSuffix(suffix = CARRIAGE_RETURN)
            val fenceMark = MarkdownSyntax.FENCE_LINE.find(input = line)?.groupValues?.get(index = 1)
            val open = fence
            when {
                open != null -> if (closesFence(line = line, fence = open)) fence = null
                fenceMark != null -> fence = fenceMark
                else -> headingOf(line = line, offset = lineStart, openLevels = openLevels)?.let { headings += it }
            }
            if (newline < 0) break
            lineStart = newline + 1
        }
        return headings
    }

    /** The heading on [line], one step deeper than the lower levels still open above it; null for any other line. */
    private fun headingOf(
        line: String,
        offset: Int,
        openLevels: ArrayDeque<Int>
    ): OutlineHeading? {
        val match = HEADING.matchEntire(input = line) ?: return null
        val marks = match.groupValues[1]
        val content = CLOSING_MARKS.replace(input = match.groupValues[2], replacement = StringPatterns.EMPTY)
        val shown = stripper.strip(line = marks + StringPatterns.SPACE + content, ranges = emptyList()).text.trim()
        if (shown.isEmpty()) return null
        val level = marks.length
        while (openLevels.lastOrNull()?.let { it >= level } == true) openLevels.removeLast()
        val heading = OutlineHeading(level = level, text = shown, offset = offset, depth = openLevels.size)
        openLevels.addLast(level)
        return heading
    }

    /** Whether [line] closes the code block [fence] opened: the same character, at least as many, and nothing else. */
    private fun closesFence(
        line: String,
        fence: String
    ): Boolean {
        val mark = MarkdownSyntax.FENCE_LINE.find(input = line)?.groupValues?.get(index = 1) ?: return false
        val rest = line.substringAfter(delimiter = mark)
        return mark.first() == fence.first() && mark.length >= fence.length && rest.isBlank()
    }

    companion object {
        private const val CARRIAGE_RETURN = "\r"
        private val HEADING = Regex(pattern = "^(#{1,6})\\s+(.*)$")
        private val CLOSING_MARKS = Regex(pattern = "(?:^|\\s+)#+\\s*$")
    }
}
