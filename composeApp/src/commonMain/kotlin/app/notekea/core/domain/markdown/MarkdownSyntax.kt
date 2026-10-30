package app.notekea.core.domain.markdown

/**
 * The marks of Markdown and Obsidian that Notekea reads and writes, named once and shared by the formatter, the list
 * keys, the editor's highlighting and the finders, so they always agree on what a fence, a tag or a list item is.
 */
object MarkdownSyntax {
    const val HEADING_MARK = "#"
    const val BOLD = "**"
    const val ITALIC = "*"
    const val ITALIC_CHAR = '*'
    const val BOLD_ITALIC = "***"

    /** A bulleted list item's marker; any of `-`, `*` and `+` is read as one. */
    const val BULLET = "- "

    /** An unchecked task's box after a list marker, and the characters a task's state can be. */
    const val TASK_BOX = "[ ] "
    const val TASK_BOX_OPEN = "["
    const val TASK_OPEN_STATE = ' '
    const val TASK_DONE_STATE = 'x'
    const val TASK_DONE_STATES = "xX"

    /** What follows a numbered item's number (`1.`); `)` is read too. */
    const val NUMBER_DELIMITER = "."
    const val QUOTE = "> "
    const val QUOTE_MARK = '>'
    const val FENCE = "```"
    const val DIVIDER = "---"
    const val TAG_MARK = "#"
    const val WIKI_LINK_OPEN = "[["
    const val WIKI_LINK_CLOSE = "]]"

    /** Between a wiki link's target and its label (`[[Note|label]]`). */
    const val LINK_LABEL_SEPARATOR = "|"
    const val LINK_LABEL_SEPARATOR_CHAR = '|'

    /** After a link's note, a heading (`#`) or a block (`^`) inside it. */
    const val HEADING_ANCHOR_CHAR = '#'
    const val BLOCK_ANCHOR_CHAR = '^'
    const val LINK_TEXT_OPEN = "["
    const val LINK_TEXT_CLOSE = "]"
    const val LINK_TARGET_OPEN = "("
    const val LINK_TARGET_CLOSE = ")"

    /** A callout's header: `[!type]`, `-` after it to start folded. */
    const val CALLOUT_OPEN = "[!"
    const val CALLOUT_CLOSE = "]"
    const val CALLOUT_FOLDED = "-"

    /** What starts a table row and separates its cells. */
    const val TABLE_COLUMN = "|"
    const val TABLE_ROW_START = "| "
    const val TABLE_CELL_SEPARATOR = " | "
    const val TABLE_ROW_END = " |"
    const val TABLE_DIVIDER_CELL = "---"
    const val WEB_ADDRESS = "https://"

    /** The opening or closing line of a fenced code block: up to three spaces, then three or more ` or ~. */
    val FENCE_LINE = Regex(pattern = "^\\s{0,3}(`{3,}|~{3,})")

    /** A tag's name: letters, digits, `_`, `-` and `/`, not only digits. */
    const val TAG_NAME_PATTERN = "[\\p{L}\\p{N}_/-]*[\\p{L}_/-][\\p{L}\\p{N}_/-]*"
    val TAG_NAME = Regex(pattern = TAG_NAME_PATTERN)

    /** A `#tag` in text, its name in group 1: a `#` not after a letter, digit, `#`, `/` or `&`. */
    val TAG = Regex(pattern = "(?<![\\w#/&])#($TAG_NAME_PATTERN)")
}
