package app.notekea.core.notes.domain

import app.notekea.core.domain.StringPatterns
import app.notekea.core.domain.lineEndAt
import app.notekea.core.domain.lineStartAt
import app.notekea.core.domain.markdown.MarkdownSyntax

/** Whether the line of [text] holding [offset] is a row of a table (`| a | b |`). */
internal fun isTableRow(
    text: String,
    offset: Int
): Boolean {
    val lineStart = text.lineStartAt(index = offset)

    val lineEnd = text.lineEndAt(index = offset)

    return text
        .substring(
            startIndex = lineStart,
            endIndex = lineEnd
        )
        .trimStart()
        .startsWith(prefix = MarkdownSyntax.TABLE_COLUMN)
}
