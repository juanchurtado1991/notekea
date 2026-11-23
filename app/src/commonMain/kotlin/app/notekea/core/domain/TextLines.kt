package app.notekea.core.domain

import app.notekea.core.domain.StringPatterns.NEW_LINE_CHAR
import app.notekea.core.domain.markdown.FenceTracker

/** Where the line holding the character at [index] starts: just after the newline before it, or 0. */
fun CharSequence.lineStartAt(index: Int): Int = lastIndexOf(
    char = NEW_LINE_CHAR,
    startIndex = index - 1
) + 1

/** Where the line holding the character at [index] ends: at its newline, or at the end of the text. */
fun CharSequence.lineEndAt(index: Int): Int = indexOf(
    char = NEW_LINE_CHAR,
    startIndex = index
).takeIf {
    it >= 0
} ?: length

/**
 * Calls [action] with the start and end of every line, the newline left out; a text ending in a newline has an empty
 * last line, and an empty text one empty line.
 */
inline fun CharSequence.forEachLine(action: (start: Int, end: Int) -> Unit) {
    var start = 0

    while (true) {
        val end = lineEndAt(index = start)
        action(start, end)
        if (end >= length) return
        start = end + 1
    }
}

/** [forEachLine] for the lines outside fenced code blocks, as [FenceTracker] reads them (fence lines are code). */
inline fun CharSequence.forEachLineOutsideCode(action: (start: Int, end: Int) -> Unit) {
    val fences = FenceTracker()

    forEachLine { start, end ->
        val isCode = fences.read(
            text = this,
            start = start,
            end = end
        )

        if (!isCode) action(start, end)
    }
}
