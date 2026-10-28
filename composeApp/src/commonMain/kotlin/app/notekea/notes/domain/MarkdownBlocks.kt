package app.notekea.notes.domain

import app.notekea.core.domain.StringPatterns
import app.notekea.core.domain.StringPatterns.BLANK_LINE
import app.notekea.core.domain.StringPatterns.NEW_LINE
import app.notekea.core.domain.StringPatterns.NEW_LINE_CHAR
import app.notekea.core.domain.markdown.MarkdownSyntax

/**
 * The blocks the formatting toolbar writes (`ROADMAP.md` 1.17): a callout, a table, a code block, a divider or an
 * embedded file, always on lines of their own, holding the selection when there is one ([MarkdownFormatter]).
 */
class MarkdownBlocks {
    /** How a block action builds its text from the selection; null for other actions. */
    fun of(
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

    /** A block on lines of its own, holding the selection if any; [build] gives its text and what to select in it. */
    fun edit(
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
}
