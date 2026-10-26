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
        is FormatAction.Table -> { _ -> table(columnNames = action.columnNames) }
        is FormatAction.CodeBlock -> { selected -> code(placeholder = action.placeholder, selected = selected) }
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

    private fun wrap(
        text: String,
        start: Int,
        end: Int,
        mark: String,
        placeholder: String
    ): TextEdit {
        if (start == end) return insert(start = start, end = end, before = mark, selected = placeholder, after = mark)
        val selected = text.substring(startIndex = start, endIndex = end)
        val isWrappedAround =
            text.regionMatches(thisOffset = start - mark.length, other = mark, otherOffset = 0, length = mark.length) &&
                text.regionMatches(thisOffset = end, other = mark, otherOffset = 0, length = mark.length) &&
                (mark != ITALIC || (text.getOrNull(index = start - 2) != '*' && text.getOrNull(index = end + 1) != '*'))
        if (isWrappedAround) {
            return insert(
                start = start - mark.length,
                end = end + mark.length,
                before = "",
                selected = selected,
                after = ""
            )
        }
        return insert(start = start, end = end, before = mark, selected = selected, after = mark)
    }

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
        columnNames: List<String>
    ): Pair<String, IntRange> {
        val header = columnNames.joinToString(separator = " | ", prefix = "| ", postfix = " |")
        val divider = columnNames.joinToString(separator = " | ", prefix = "| ", postfix = " |") { "---" }
        val row = columnNames.joinToString(separator = " | ", prefix = "| ", postfix = " |") { " " }
        val firstCell = header.length + 1 + divider.length + 1 + 2
        return listOf(header, divider, row, row).joinToString(separator = "\n") to (firstCell until firstCell)
    }

    private fun code(
        placeholder: String,
        selected: String?
    ): Pair<String, IntRange> {
        val body = selected ?: placeholder
        val start = FENCE.length + 1
        return FENCE + "\n" + body + "\n" + FENCE to
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

    private fun tag(
        text: String,
        start: Int,
        end: Int,
        action: FormatAction.Tag
    ): TextEdit {
        val space = if (start > 0 && !text[start - 1].isWhitespace()) " " else ""
        val selected = text.substring(
            startIndex = start,
            endIndex = end
        ).trim().replace(regex = SPACES, replacement = "-")
        val name = action.name.ifBlank { selected }
        if (name.isEmpty()) {
            return insert(
                start = start,
                end = end,
                before = "$space#",
                selected = action.placeholder,
                after = ""
            )
        }
        val tag = "$space#$name "
        return TextEdit(
            start = start,
            end = end,
            replacement = tag,
            selectionStart = start + tag.length,
            selectionEnd =
            start + tag.length
        )
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
        private val SPACES = Regex(pattern = "\\s+")
    }
}
