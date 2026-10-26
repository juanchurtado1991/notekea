package app.notekea.notes.domain

import app.notekea.core.domain.StringPatterns
import app.notekea.core.domain.StringPatterns.BLANK_LINE
import app.notekea.core.domain.StringPatterns.NEW_LINE
import app.notekea.core.domain.StringPatterns.NEW_LINE_CHAR
import app.notekea.core.domain.markdown.MarkdownSyntax

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
        if (action is FormatAction.Embed && isTableRow(text = text, offset = start)) {
            // In a table the file goes into the cell, its `|` escaped so the row keeps its columns.
            val markdown = action.markdown.replace(
                oldValue = MarkdownSyntax.LINK_LABEL_SEPARATOR,
                newValue = ESCAPED_SEPARATOR
            )
            return TextEdit(start, end, markdown, start + markdown.length, start + markdown.length)
        }
        blockOf(action = action)?.let { build ->
            val edit = block(text = text, start = start, end = end, build = build)
            // After an embedded file, typing goes on below it.
            val after = edit.start + edit.replacement.length
            return if (action is FormatAction.Embed) edit.copy(selectionStart = after, selectionEnd = after) else edit
        }
        return inline(text = text, start = start, end = end, action = action)
    }

    /** Whether the line holding [offset] is a row of a table (`| a | b |`). */
    private fun isTableRow(
        text: String,
        offset: Int
    ): Boolean {
        val lineStart = text.lastIndexOf(char = NEW_LINE_CHAR, startIndex = offset - 1) + 1
        val lineEnd = text.indexOf(char = NEW_LINE_CHAR, startIndex = offset).takeIf { it >= 0 } ?: text.length
        return text.substring(
            startIndex = lineStart,
            endIndex = lineEnd
        ).trimStart().startsWith(prefix = MarkdownSyntax.TABLE_COLUMN)
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

        is FormatAction.Embed -> { _ -> action.markdown to (action.markdown.length until action.markdown.length) }

        FormatAction.Divider -> { _ ->
            MarkdownSyntax.DIVIDER to
                (MarkdownSyntax.DIVIDER.length until MarkdownSyntax.DIVIDER.length)
        }

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
            mark = MarkdownSyntax.BOLD,
            placeholder = action.placeholder
        )

        is FormatAction.Italic -> wrap(
            text = text,
            start = start,
            end = end,
            mark = MarkdownSyntax.ITALIC,
            placeholder = action.placeholder
        )

        is FormatAction.NoteLink -> noteLink(text = text, start = start, end = end, target = action.target)

        is FormatAction.WebLink -> webLink(text = text, start = start, end = end, placeholder = action.placeholder)

        is FormatAction.Tag -> tag(text = text, start = start, end = end, action = action)

        else -> error(NOT_INLINE_MESSAGE + action)
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
            return insert(start = from, end = to, selected = inner)
        }
        if (isMarkedAround(text = text, start = from, end = to, mark = mark)) {
            return insert(start = from - mark.length, end = to + mark.length, selected = selected)
        }
        return insert(start = from, end = to, before = mark, selected = selected, after = mark)
    }

    /** The word [offset] is inside (letters or digits on both sides), or null between words. */
    private fun wordAround(
        text: String,
        offset: Int
    ): IntRange? {
        val isWord = { char: Char -> char.isLetterOrDigit() || char == StringPatterns.UNDERSCORE_CHAR }
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
        val isBoldOnly = mark == MarkdownSyntax.ITALIC && selected.startsWith(prefix = MarkdownSyntax.BOLD) &&
            !selected.startsWith(prefix = MarkdownSyntax.BOLD_ITALIC)
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
            (
                mark != MarkdownSyntax.ITALIC || (
                    text.getOrNull(index = start - 2) != MarkdownSyntax.ITALIC_CHAR &&
                        text.getOrNull(index = end + 1) != MarkdownSyntax.ITALIC_CHAR
                    )
                )

    /** [style] on every line the selection touches, or off when every non-empty one already has it. */
    private fun styleLines(
        text: String,
        start: Int,
        end: Int,
        style: LineStyle,
        placeholder: String
    ): TextEdit {
        val lineStart = text.lastIndexOf(char = NEW_LINE_CHAR, startIndex = start - 1) + 1
        val lastLineEnd = if (end > start && text.getOrNull(index = end - 1) == NEW_LINE_CHAR) end - 1 else end
        val lineEnd = text.indexOf(char = NEW_LINE_CHAR, startIndex = lastLineEnd).takeIf { it >= 0 } ?: text.length
        val lines = text.substring(startIndex = lineStart, endIndex = lineEnd).split(NEW_LINE_CHAR)
        if (start == end && lines.single().isBlank()) {
            return insert(
                start = lineStart,
                end = lineEnd,
                before = style.add(line = StringPatterns.EMPTY, number = 1),
                selected = placeholder
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
        val replacement = styled.joinToString(separator = NEW_LINE)
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
            start == 0 -> StringPatterns.EMPTY
            text[start - 1] != NEW_LINE_CHAR -> BLANK_LINE
            start >= 2 && text[start - 2] != NEW_LINE_CHAR -> NEW_LINE
            else -> StringPatterns.EMPTY
        }
        val rest = text.substring(startIndex = end)
        val after = when {
            rest.startsWith(prefix = BLANK_LINE) -> StringPatterns.EMPTY
            rest.startsWith(prefix = NEW_LINE) || rest.isEmpty() -> NEW_LINE
            else -> BLANK_LINE
        }
        val (content, selection) = build(selected)
        val offset = start + before.length
        return TextEdit(start, end, before + content + after, offset + selection.first, offset + selection.last + 1)
    }

    private fun callout(
        action: FormatAction.Callout,
        selected: String?
    ): Pair<String, IntRange> {
        val fold = if (action.isFolded) MarkdownSyntax.CALLOUT_FOLDED else StringPatterns.EMPTY
        val header = MarkdownSyntax.QUOTE + MarkdownSyntax.CALLOUT_OPEN + action.type + MarkdownSyntax.CALLOUT_CLOSE +
            fold + StringPatterns.SPACE + action.title + NEW_LINE
        val body = (selected ?: action.placeholder).split(NEW_LINE_CHAR).joinToString(separator = NEW_LINE) {
            MarkdownSyntax.QUOTE + it
        }
        val bodyStart = header.length + MarkdownSyntax.QUOTE.length
        val selectedLength = if (selected == null) action.placeholder.length else 0
        return header + body to (bodyStart until bodyStart + selectedLength)
    }

    private fun table(
        action: FormatAction.Table
    ): Pair<String, IntRange> {
        val line = { cells: List<String> ->
            cells.joinToString(
                separator = MarkdownSyntax.TABLE_CELL_SEPARATOR,
                prefix = MarkdownSyntax.TABLE_ROW_START,
                postfix = MarkdownSyntax.TABLE_ROW_END
            )
        }
        val divider = line(action.columnNames.map { MarkdownSyntax.TABLE_DIVIDER_CELL })
        val row = line(action.columnNames.map { action.cellText })
        val lines = listOf(line(action.columnNames), divider) + List(size = action.rows) { row }
        val headerStart = MarkdownSyntax.TABLE_ROW_START.length
        val firstHeader = action.columnNames.firstOrNull().orEmpty()
        return lines.joinToString(separator = NEW_LINE) to (headerStart until headerStart + firstHeader.length)
    }

    private fun code(
        action: FormatAction.CodeBlock,
        selected: String?
    ): Pair<String, IntRange> {
        val body = selected ?: action.placeholder
        val opening = MarkdownSyntax.FENCE + action.language
        val start = opening.length + NEW_LINE.length
        val selectedLength = if (selected == null) body.length else 0
        return opening + NEW_LINE + body + NEW_LINE + MarkdownSyntax.FENCE to (start until start + selectedLength)
    }

    private fun noteLink(
        text: String,
        start: Int,
        end: Int,
        target: String
    ): TextEdit {
        val label = text.substring(startIndex = start, endIndex = end).takeIf { it.isNotBlank() && it != target }
        val labelPart = label?.let { MarkdownSyntax.LINK_LABEL_SEPARATOR + it }.orEmpty()
        val link = MarkdownSyntax.WIKI_LINK_OPEN + target + labelPart + MarkdownSyntax.WIKI_LINK_CLOSE
        return TextEdit(start, end, link, start + link.length, start + link.length)
    }

    /** `[text](https://)`: the selection or [placeholder] as the text, the address selected when there is text. */
    private fun webLink(
        text: String,
        start: Int,
        end: Int,
        placeholder: String
    ): TextEdit {
        val selected = text.substring(startIndex = start, endIndex = end)
        val target = MarkdownSyntax.LINK_TEXT_CLOSE + MarkdownSyntax.LINK_TARGET_OPEN + MarkdownSyntax.WEB_ADDRESS +
            MarkdownSyntax.LINK_TARGET_CLOSE
        if (selected.isEmpty()) {
            return insert(
                start = start,
                end = end,
                before = MarkdownSyntax.LINK_TEXT_OPEN,
                selected = placeholder,
                after = target
            )
        }
        val link = MarkdownSyntax.LINK_TEXT_OPEN + selected + target
        val addressStart = start + MarkdownSyntax.LINK_TEXT_OPEN.length + selected.length +
            MarkdownSyntax.LINK_TEXT_CLOSE.length + MarkdownSyntax.LINK_TARGET_OPEN.length
        return TextEdit(start, end, link, addressStart, addressStart + MarkdownSyntax.WEB_ADDRESS.length)
    }

    /** The tag in place of the selection, with a space before it when needed and one after it to keep typing. */
    private fun tag(
        text: String,
        start: Int,
        end: Int,
        action: FormatAction.Tag
    ): TextEdit {
        val space = if (start > 0 && !text[start - 1].isWhitespace()) StringPatterns.SPACE else StringPatterns.EMPTY
        val tag = space + MarkdownSyntax.TAG_MARK + action.name + StringPatterns.SPACE
        return TextEdit(start, end, tag, start + tag.length, start + tag.length)
    }

    /** [selected] between [before] and [after] in place of [start] until [end], with [selected] selected. */
    private fun insert(
        start: Int,
        end: Int,
        selected: String,
        before: String = StringPatterns.EMPTY,
        after: String = StringPatterns.EMPTY
    ): TextEdit = TextEdit(
        start = start,
        end = end,
        replacement = before + selected + after,
        selectionStart = start + before.length,
        selectionEnd = start + before.length + selected.length
    )

    companion object {
        private const val ESCAPED_SEPARATOR = "\\|"
        private const val NOT_INLINE_MESSAGE = "Not an inline action: "
    }
}
