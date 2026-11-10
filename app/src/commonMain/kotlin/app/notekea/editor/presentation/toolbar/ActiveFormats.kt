package app.notekea.editor.presentation.toolbar

import app.notekea.core.domain.StringPatterns.NEW_LINE_CHAR
import app.notekea.editor.presentation.markdown.HighlightKind
import app.notekea.editor.presentation.markdown.MarkdownHighlighter

/**
 * The toolbar's formats the cursor is in, read from its line alone so that typing in a long note stays cheap: a line
 * of a code block is read as text.
 */
class ActiveFormats {
    private val highlighter = MarkdownHighlighter()

    /** The formats at [cursor] in [text]: the ones of the words it is in or beside, and its line's list or heading. */
    fun at(
        text: CharSequence,
        cursor: Int
    ): Set<ToolbarFormat> {
        val position = cursor.coerceIn(
            minimumValue = 0,
            maximumValue = text.length
        )

        val lineStart = text.lastIndexOf(
            char = NEW_LINE_CHAR,
            startIndex = position - 1
        ) + 1

        val lineEnd = text
            .indexOf(
                char = NEW_LINE_CHAR,
                startIndex = position
            )
            .takeIf {
                it >= 0
            }
            ?: text.length

        val line = text.substring(
            startIndex = lineStart,
            endIndex = lineEnd
        )

        val column = position - lineStart

        val inline = highlighter
            .highlight(text = line)
            .filter {
                column in it.start..it.end
            }
            .mapNotNull {
                FORMATS[it.kind]
            }

        val list = when {
            CHECKLIST.containsMatchIn(input = line) -> ToolbarFormat.CHECKLIST
            BULLET_LIST.containsMatchIn(input = line) -> ToolbarFormat.BULLET_LIST
            else -> null
        }

        return (inline + listOfNotNull(list)).toSet()
    }

    private companion object {
        val BULLET_LIST = Regex(pattern = """^\s*[-*+] """)
        val CHECKLIST = Regex(pattern = """^\s*[-*+] \[[ xX]] """)

        val FORMATS = mapOf(
            HighlightKind.BOLD to ToolbarFormat.BOLD,
            HighlightKind.ITALIC to ToolbarFormat.ITALIC,
            HighlightKind.HEADING_1 to ToolbarFormat.HEADING,
            HighlightKind.HEADING_2 to ToolbarFormat.HEADING,
            HighlightKind.HEADING_3 to ToolbarFormat.HEADING,
            HighlightKind.HEADING_4 to ToolbarFormat.HEADING,
            HighlightKind.HEADING_5 to ToolbarFormat.HEADING,
            HighlightKind.HEADING_6 to ToolbarFormat.HEADING,
            HighlightKind.WIKI_LINK to ToolbarFormat.NOTE_LINK,
            HighlightKind.TAG to ToolbarFormat.TAG
        )
    }
}
