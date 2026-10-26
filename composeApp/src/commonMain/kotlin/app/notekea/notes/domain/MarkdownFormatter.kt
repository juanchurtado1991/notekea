package app.notekea.notes.domain

/**
 * What a formatting toolbar button does to a note's text (`ROADMAP.md` 1.17), as the single [TextEdit] a person would
 * type. With a selection, it formats it: bold and italic wrap it (or unwrap it when it already is), line styles apply
 * to every selected line (or are removed when all have them), blocks hold it. Without one, it inserts an example with
 * the placeholder selected, ready to type over. Blocks (callout, table, code, divider) always sit on lines of their own.
 */
class MarkdownFormatter {
    fun format(
        text: String,
        selectionStart: Int,
        selectionEnd: Int,
        action: FormatAction
    ): TextEdit {
        val start = minOf(selectionStart, selectionEnd).coerceIn(range = 0..text.length)
        val end = maxOf(selectionStart, selectionEnd).coerceIn(range = 0..text.length)
        lineStyleOf(action = action)?.let { (style, placeholder) ->
            return styleLines(text = text, start = start, end = end, style = style, placeholder = placeholder)
        }
        blockOf(action = action)?.let { build -> return block(text = text, start = start, end = end, build = build) }
        return inline(text = text, start = start, end = end, action = action)
    }

    /** The line style a line action applies, with its placeholder; null for other actions. */
    private fun lineStyleOf(
        action: FormatAction
    ): Pair<LineStyle, String>? = when (action) {
        is FormatAction.Heading -> LineStyle.Heading(level = action.level) to action.placeholder
        is FormatAction.BulletList -> LineStyle.Bullet to action.placeholder
        is FormatAction.NumberedList -> LineStyle.Numbered to action.placeholder
        is FormatAction.Checklist -> LineStyle.Checklist to action.placeholder
        is FormatAction.Quote -> LineStyle.Quote to action.placeholder
        else -> null
    }

    /** How a block action builds its text from the selection; null for other actions. */
    private fun blockOf(
        action: FormatAction
    ): ((String?) -> Pair<String, IntRange>)? = when (action) {
        is FormatAction.Callout -> { selected -> callout(action = action, selected = selected) }
        is FormatAction.Table -> { _ -> table(action = action) }
        is FormatAction.CodeBlock -> { selected -> code(action = action, selected = selected) }
        FormatAction.Divider -> { _ -> DIVIDER to (DIVIDER.length until DIVIDER.length) }
        else -> null
    }

    private fun inline(
        text: String,
        start: Int,
        end: Int,
        action: FormatAction
    ): TextEdit = when (action) {
        is FormatAction.Bold -> wrap(
            text = text,
            start = start,
            end = end,
            mark = BOLD,
            placeholder = action.placeholder
        )

        is FormatAction.Italic -> wrap(
            text = text,
            start = start,
            end = end,
            mark = ITALIC,
            placeholder = action.placeholder
        )

        is FormatAction.NoteLink -> noteLink(text = text, start = start, end = end, target = action.target)

        is FormatAction.WebLink -> webLink(text = text, start = start, end = end, placeholder = action.placeholder)

        is FormatAction.Tag -> tag(text = text, start = start, end = end, action = action)

        else -> error("Not an inline action: $action")
    }

    /**
     * Bold or italic: wraps the selection, or the word the cursor is in, and unwraps it when the marks are around it
     * or selected with it; with the cursor outside a word it inserts [placeholder] to type over.
     */
    private fun wrap(
        text: String,
        start: Int,
        end: Int,
        mark: String,
        placeholder: String
    ): TextEdit {
        val word = if (start == end) wordAround(text = text, offset = start) else start until end
        if (word == null) return insert(start = start, end = end, before = mark, selected = placeholder, after = mark)
        val from = word.first
        val to = word.last + 1
        val selected = text.substring(startIndex = from, endIndex = to)
        if (isMarkedInside(selected = selected, mark = mark)) {
            val inner = selected.substring(startIndex = mark.length, endIndex = selected.length - mark.length)
            return insert(start = from, end = to, before = "", selected = inner, after = "")
        }
        if (isMarkedAround(text = text, start = from, end = to, mark = mark)) {
            return insert(
                start = from - mark.length,
                end = to + mark.length,
                before = "",
                selected = selected,
                after = ""
            )
        }
        return insert(start = from, end = to, before = mark, selected = selected, after = mark)
    }

    /** The word [offset] is inside (letters or digits on both sides), or null between words. */
    private fun wordAround(
        text: String,
        offset: Int
    ): IntRange? {
        val isWord = { char: Char -> char.isLetterOrDigit() || char == '_' }
        if (offset == 0 || offset == text.length || !isWord(text[offset - 1]) || !isWord(text[offset])) return null
        var from = offset
        while (from > 0 && isWord(text[from - 1])) from--
        var to = offset
        while (to < text.length && isWord(text[to])) to++
        return from until to
    }

    /** Whether the selection itself starts and ends with [mark] (`**corn**` selected whole), italic not taking bold. */
    private fun isMarkedInside(
        selected: String,
        mark: String
    ): Boolean {
        val isMarked = selected.length > 2 * mark.length && selected.startsWith(prefix = mark) &&
            selected.endsWith(suffix = mark)
        val isBoldOnly = mark == ITALIC && selected.startsWith(prefix = BOLD) && !selected.startsWith(prefix = "***")
        return isMarked && !isBoldOnly
    }

    private fun isMarkedAround(
        text: String,
        start: Int,
        end: Int,
        mark: String
    ): Boolean =
        text.regionMatches(thisOffset = start - mark.length, other = mark, otherOffset = 0, length = mark.length) &&
            text.regionMatches(thisOffset = end, other = mark, otherOffset = 0, length = mark.length) &&
            (mark != ITALIC || (text.getOrNull(index = start - 2) != '*' && text.getOrNull(index = end + 1) != '*'))

    /** [style] on every line the selection touches, or off when every non-empty one already has it. */
    private fun styleLines(
        text: String,
        start: Int,
        end: Int,
        style: LineStyle,
        placeholder: String
    ): TextEdit {
        val lineStart = text.lastIndexOf(char = '\n', startIndex = start - 1) + 1
        val lastLineEnd = if (end > start && text.getOrNull(index = end - 1) == '\n') end - 1 else end
        val lineEnd = text.indexOf(char = '\n', startIndex = lastLineEnd).takeIf { it >= 0 } ?: text.length
        val lines = text.substring(startIndex = lineStart, endIndex = lineEnd).split('\n')
        if (start == end && lines.single().isBlank()) {
            return insert(
                start = lineStart,
                end = lineEnd,
                before = style.add(line = "", number = 1),
                selected = placeholder,
                after = ""
            )
        }
        val filled = lines.filter { it.isNotBlank() }
        val isOn = filled.isNotEmpty() && filled.all { style.matches(line = it) }
        var number = 0
        val styled = lines.map { line ->
            when {
                line.isBlank() -> line
                isOn -> style.remove(line = line)
                else -> style.add(line = line, number = ++number)
            }
        }
        val replacement = styled.joinToString(separator = "\n")
        if (start != end) return TextEdit(lineStart, lineEnd, replacement, lineStart, lineStart + replacement.length)
        val cursor = (start + styled.first().length - lines.first().length).coerceAtLeast(minimumValue = lineStart)
        return TextEdit(
            start = lineStart,
            end = lineEnd,
            replacement = replacement,
            selectionStart = cursor,
            selectionEnd = cursor
        )
    }

    /** A block on lines of its own, holding the selection if any; [build] gives its text and what to select in it. */
    private fun block(
        text: String,
        start: Int,
        end: Int,
        build: (String?) -> Pair<String, IntRange>
    ): TextEdit {
        val selected = text.substring(startIndex = start, endIndex = end).takeIf { it.isNotEmpty() }
        val before = when {
            start == 0 -> ""
            text[start - 1] != '\n' -> "\n\n"
            start >= 2 && text[start - 2] != '\n' -> "\n"
            else -> ""
        }
        val rest = text.substring(startIndex = end)
        val after = when {
            rest.startsWith(prefix = "\n\n") -> ""
            rest.startsWith(prefix = "\n") || rest.isEmpty() -> "\n"
            else -> "\n\n"
        }
        val (content, selection) = build(selected)
        val offset = start + before.length
        return TextEdit(start, end, before + content + after, offset + selection.first, offset + selection.last + 1)
    }

    private fun callout(
        action: FormatAction.Callout,
        selected: String?
    ): Pair<String, IntRange> {
        val header = "> [!" + action.type + "]" + (if (action.isFolded) "-" else "") + " " + action.title + "\n"
        val body = (selected ?: action.placeholder).split('\n').joinToString(separator = "\n") { "> $it" }
        val bodyStart = header.length + QUOTE_PREFIX.length
        val selection = if (selected ==
            null
        ) {
            bodyStart until bodyStart + action.placeholder.length
        } else {
            bodyStart until bodyStart
        }
        return header + body to selection
    }

    private fun table(
        action: FormatAction.Table
    ): Pair<String, IntRange> {
        val line = { cells: List<String> -> cells.joinToString(separator = " | ", prefix = "| ", postfix = " |") }
        val divider = line(action.columnNames.map { "---" })
        val row = line(action.columnNames.map { action.cellText })
        val lines = listOf(line(action.columnNames), divider) + List(size = action.rows) { row }
        val firstHeader = action.columnNames.firstOrNull().orEmpty()
        return lines.joinToString(separator = "\n") to (2 until 2 + firstHeader.length)
    }

    private fun code(
        action: FormatAction.CodeBlock,
        selected: String?
    ): Pair<String, IntRange> {
        val body = selected ?: action.placeholder
        val opening = FENCE + action.language
        val start = opening.length + 1
        return opening + "\n" + body + "\n" + FENCE to
            (if (selected == null) start until start + body.length else start until start)
    }

    private fun noteLink(
        text: String,
        start: Int,
        end: Int,
        target: String
    ): TextEdit {
        val label = text.substring(startIndex = start, endIndex = end).takeIf { it.isNotBlank() && it != target }
        val link = "[[" + target + (label?.let { "|$it" }.orEmpty()) + "]]"
        return TextEdit(
            start = start,
            end = end,
            replacement = link,
            selectionStart = start + link.length,
            selectionEnd =
            start + link.length
        )
    }

    private fun webLink(
        text: String,
        start: Int,
        end: Int,
        placeholder: String
    ): TextEdit {
        val selected = text.substring(startIndex = start, endIndex = end)
        if (selected.isEmpty()) {
            return insert(
                start = start,
                end = end,
                before = "[",
                selected = placeholder,
                after = "]($WEB_ADDRESS)"
            )
        }
        val addressStart = start + selected.length + 3
        return TextEdit(start, end, "[$selected]($WEB_ADDRESS)", addressStart, addressStart + WEB_ADDRESS.length)
    }

    /** The tag in place of the selection, with a space before it when needed and one after it to keep typing. */
    private fun tag(
        text: String,
        start: Int,
        end: Int,
        action: FormatAction.Tag
    ): TextEdit {
        val space = if (start > 0 && !text[start - 1].isWhitespace()) " " else ""
        val tag = "$space#${action.name} "
        return TextEdit(start, end, tag, start + tag.length, start + tag.length)
    }

    /** [selected] between [before] and [after] in place of [start] until [end], with [selected] selected. */
    private fun insert(
        start: Int,
        end: Int,
        before: String,
        selected: String,
        after: String
    ): TextEdit = TextEdit(
        start = start,
        end = end,
        replacement = before + selected + after,
        selectionStart = start + before.length,
        selectionEnd = start + before.length + selected.length
    )

    companion object {
        private const val BOLD = "**"
        private const val DIVIDER = "---"
        private const val FENCE = "```"
        private const val ITALIC = "*"
        private const val QUOTE_PREFIX = "> "
        private const val WEB_ADDRESS = "https://"
    }
}
