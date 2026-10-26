package app.notekea.notes.domain

/**
 * What a formatting toolbar button asks for (`ROADMAP.md` 1.17). Each carries the placeholder text inserted when
 * nothing is selected, already in the person's language, so [MarkdownFormatter] stays free of words.
 */
sealed interface FormatAction {
    data class Bold(val placeholder: String) : FormatAction

    data class Italic(val placeholder: String) : FormatAction

    /** A heading of [level] 1 (large) to 3 (small). */
    data class Heading(
        val level: Int,
        val placeholder: String
    ) : FormatAction

    data class BulletList(val placeholder: String) : FormatAction

    data class NumberedList(val placeholder: String) : FormatAction

    data class Checklist(val placeholder: String) : FormatAction

    data class Quote(val placeholder: String) : FormatAction

    /** An Obsidian callout of [type] (as written after `[!`), titled [title], closed when [isFolded]. */
    data class Callout(
        val type: String,
        val title: String,
        val placeholder: String,
        val isFolded: Boolean
    ) : FormatAction

    /** A 3 × 3 table whose header cells are [columnNames]. */
    data class Table(val columnNames: List<String>) : FormatAction

    data class CodeBlock(val placeholder: String) : FormatAction

    data object Divider : FormatAction

    /** A wiki link to [target], the note's name (or path when its name is not unique). */
    data class NoteLink(val target: String) : FormatAction

    data class WebLink(val placeholder: String) : FormatAction

    /** A `#tag`; an empty [name] inserts `#` and the [placeholder] to type over. */
    data class Tag(
        val name: String,
        val placeholder: String
    ) : FormatAction
}
