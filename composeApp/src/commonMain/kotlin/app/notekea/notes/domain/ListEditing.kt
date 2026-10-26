package app.notekea.notes.domain

/**
 * Lists and quotes behave as in other editors (`ROADMAP.md` 1.17), as one [TextEdit] per key, or null where the key
 * keeps its usual meaning (plain text, code blocks, the cursor inside a marker, a selection for Enter):
 * - Enter on an item adds the next item (the next number, an unchecked task), carrying the text after the cursor; on
 *   an empty item it ends the list. A quote line continues the quote.
 * - Tab makes the selected items children of the item above (indented to its text, as Markdown nests them) and
 *   Shift+Tab takes them back to the level of their parent.
 * - Backspace right after a marker removes the marker.
 */
class ListEditing {
    fun edit(
        text: String,
        selectionStart: Int,
        selectionEnd: Int,
        action: ListAction
    ): TextEdit? {
        val start = minOf(selectionStart, selectionEnd).coerceIn(range = 0..text.length)
        val end = maxOf(selectionStart, selectionEnd).coerceIn(range = 0..text.length)
        val lines = text.split('\n')
        val first = lineIndexOf(text = text, offset = start)
        if (isInCode(lines = lines, index = first)) return null
        return when (action) {
            ListAction.NEW_ITEM -> if (start == end) newItem(text = text, cursor = start) else null
            ListAction.INDENT -> shift(lines = lines, start = start, end = end, isIndent = true)
            ListAction.OUTDENT -> shift(lines = lines, start = start, end = end, isIndent = false)
            ListAction.REMOVE_MARKER -> if (start == end) removeMarker(text = text, cursor = start) else null
        }
    }

    private fun newItem(
        text: String,
        cursor: Int
    ): TextEdit? {
        val lineStart = text.lastIndexOf(char = '\n', startIndex = cursor - 1) + 1
        val lineEnd = text.indexOf(char = '\n', startIndex = cursor).takeIf { it >= 0 } ?: text.length
        val line = ListLine.of(line = text.substring(startIndex = lineStart, endIndex = lineEnd))
        val markerEnd = lineStart + line.prefix.length + line.marker.length
        if ((!line.isItem && !line.isQuote) || cursor < markerEnd) return null
        if (line.content.isBlank()) return TextEdit(lineStart, lineEnd, "", lineStart, lineStart)
        val next = "\n" + line.prefix + (if (line.isItem) line.nextMarker() else "")
        return TextEdit(cursor, cursor, next, cursor + next.length, cursor + next.length)
    }

    private fun removeMarker(
        text: String,
        cursor: Int
    ): TextEdit? {
        val lineStart = text.lastIndexOf(char = '\n', startIndex = cursor - 1) + 1
        val lineEnd = text.indexOf(char = '\n', startIndex = cursor).takeIf { it >= 0 } ?: text.length
        val line = ListLine.of(line = text.substring(startIndex = lineStart, endIndex = lineEnd))
        val markerStart = lineStart + line.prefix.length
        if (!line.isItem || cursor != markerStart + line.marker.length) return null
        return TextEdit(markerStart, cursor, "", markerStart, markerStart)
    }

    /** Tab and Shift+Tab: every list item the selection touches moves by the step the first one takes. */
    private fun shift(
        lines: List<String>,
        start: Int,
        end: Int,
        isIndent: Boolean
    ): TextEdit? {
        val text = lines.joinToString(separator = "\n")
        val first = lineIndexOf(text = text, offset = start)
        val last = lineIndexOf(text = text, offset = if (end > start && text[end - 1] == '\n') end - 1 else end)
        val items = (first..last).filter { ListLine.of(line = lines[it]).isItem }
        if (items.isEmpty()) return null
        val firstItem = ListLine.of(line = lines[items.first()])
        val target = if (isIndent) {
            childIndent(lines = lines, index = items.first())
        } else {
            parentIndent(lines = lines, index = items.first())
        } ?: firstItem.indent
        val step = target - firstItem.indent
        val shifted = lines.mapIndexed { index, line ->
            if (index in
                items
            ) {
                shiftLine(line = line, step = step)
            } else {
                line
            }
        }
        val firstStart = lineStartOf(lines = lines, index = first)
        val lastEnd = lineStartOf(lines = lines, index = last) + lines[last].length
        val replacement = shifted.subList(fromIndex = first, toIndex = last + 1).joinToString(separator = "\n")
        val newStart = mapOffset(lines = lines, shifted = shifted, offset = start, items = items, step = step)
        val newEnd = mapOffset(lines = lines, shifted = shifted, offset = end, items = items, step = step)
        return TextEdit(firstStart, lastEnd, replacement, newStart, newEnd)
    }

    /** Where an item becomes a child: under the text of the nearest item above at its level; null when there is none. */
    private fun childIndent(
        lines: List<String>,
        index: Int
    ): Int? {
        val item = ListLine.of(line = lines[index])
        val above = itemsAbove(lines = lines, index = index).firstOrNull { it.indent <= item.indent } ?: return null
        return if (above.indent == item.indent) above.indent + above.textOffset else null
    }

    /** The level of the item's parent: the nearest item above that is less indented, or the left margin. */
    private fun parentIndent(
        lines: List<String>,
        index: Int
    ): Int {
        val item = ListLine.of(line = lines[index])
        return itemsAbove(lines = lines, index = index).firstOrNull { it.indent < item.indent }?.indent ?: 0
    }

    /** The items above [index] of the same list, nearest first: the list ends at a line that is not an item. */
    private fun itemsAbove(
        lines: List<String>,
        index: Int
    ): List<ListLine> = (index - 1 downTo 0)
        .map { ListLine.of(line = lines[it]) }
        .takeWhile { it.isItem }

    private fun shiftLine(
        line: String,
        step: Int
    ): String {
        if (step >= 0) return " ".repeat(n = step) + line
        val removable = line.takeWhile { it == ' ' || it == '\t' }.length
        return line.substring(startIndex = minOf(-step, removable))
    }

    /** [offset] in the text after the shift: moved with its line, and never into the indentation it lost. */
    private fun mapOffset(
        lines: List<String>,
        shifted: List<String>,
        offset: Int,
        items: List<Int>,
        step: Int
    ): Int {
        val text = lines.joinToString(separator = "\n")
        val index = lineIndexOf(text = text, offset = offset)
        val column = offset - lineStartOf(lines = lines, index = index)
        val moved = if (index in items) column + (shifted[index].length - lines[index].length) else column
        val bounded = if (index in items && step < 0) moved.coerceAtLeast(minimumValue = 0) else moved
        return lineStartOf(lines = shifted, index = index) + bounded.coerceIn(range = 0..shifted[index].length)
    }

    private fun lineIndexOf(
        text: String,
        offset: Int
    ): Int = text.substring(startIndex = 0, endIndex = offset).count { it == '\n' }

    private fun lineStartOf(
        lines: List<String>,
        index: Int
    ): Int = lines.take(n = index).sumOf { it.length + 1 }

    /** Whether line [index] is inside a fenced code block, where list keys type as usual. */
    private fun isInCode(
        lines: List<String>,
        index: Int
    ): Boolean {
        var fence: Char? = null
        for (line in lines.take(n = index + 1)) {
            val marker = FENCE.find(input = line)?.groupValues?.get(index = 1)?.first()
            fence = when {
                fence != null -> if (marker == fence) null else fence
                else -> marker
            }
        }
        return fence != null && FENCE.find(input = lines[index]) == null
    }

    companion object {
        private val FENCE = Regex(pattern = "^\\s{0,3}(`{3,}|~{3,})")
    }
}
