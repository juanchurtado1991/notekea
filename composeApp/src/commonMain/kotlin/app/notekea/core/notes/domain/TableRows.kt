package app.notekea.core.notes.domain

import app.notekea.core.domain.StringPatterns
import app.notekea.core.domain.markdown.MarkdownSyntax

/** Whether the line of [text] holding [offset] is a row of a table (`| a | b |`). */
internal fun isTableRow(
    text: String,
    offset: Int
): Boolean {
    val lineStart = text.lastIndexOf(char = StringPatterns.NEW_LINE_CHAR, startIndex = offset - 1) + 1
    val lineEnd =
        text.indexOf(char = StringPatterns.NEW_LINE_CHAR, startIndex = offset).takeIf { it >= 0 } ?: text.length
    return text.substring(
        startIndex = lineStart,
        endIndex = lineEnd
    ).trimStart().startsWith(prefix = MarkdownSyntax.TABLE_COLUMN)
}
