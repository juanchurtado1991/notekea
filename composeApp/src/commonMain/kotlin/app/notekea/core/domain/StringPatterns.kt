package app.notekea.core.domain

/**
 * The plain text pieces code splits, joins and builds text with, named once so no line of logic holds a bare `" "` or
 * `'\n'`. Markdown's own marks live in [app.notekea.core.domain.markdown.MarkdownSyntax].
 */
object StringPatterns {
    const val EMPTY = ""
    const val SPACE = " "
    const val SPACE_CHAR = ' '
    const val TAB_CHAR = '\t'
    const val NEW_LINE = "\n"
    const val NEW_LINE_CHAR = '\n'

    /** An empty line between two blocks of text. */
    const val BLANK_LINE = "\n\n"
    const val WINDOWS_NEW_LINE = "\r\n"
    const val HYPHEN = "-"
    const val UNDERSCORE_CHAR = '_'
    const val ZERO_CHAR = '0'

    /** One or more whitespace characters in a row. */
    val WHITESPACE = Regex(pattern = "\\s+")

    /** Whether [char] is a space or a tab, the characters that indent a line. */
    fun isIndent(
        char: Char
    ): Boolean = char == SPACE_CHAR || char == TAB_CHAR
}
