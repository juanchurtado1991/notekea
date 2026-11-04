package app.notekea.core.notes.domain

import app.notekea.core.domain.StringPatterns
import app.notekea.core.domain.markdown.FenceTracker

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
        val fences = FenceTracker()
        var lineStart = 0
        while (lineStart <= text.length) {
            val newline = text.indexOf(char = StringPatterns.NEW_LINE_CHAR, startIndex = lineStart)
            val lineEnd = if (newline < 0) text.length else newline
            val line = text.substring(startIndex = lineStart, endIndex = lineEnd).removeSuffix(suffix = CARRIAGE_RETURN)
            if (!fences.read(text = line)) {
                headingOf(line = line, offset = lineStart, openLevels = openLevels)?.let { headings += it }
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

    companion object {
        private const val CARRIAGE_RETURN = "\r"
        private val HEADING = Regex(pattern = "^(#{1,6})\\s+(.*)$")
        private val CLOSING_MARKS = Regex(pattern = "(?:^|\\s+)#+\\s*$")
    }
}
