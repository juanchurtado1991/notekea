package app.notekea.editor.domain

import app.notekea.core.notes.domain.RelativePath

/**
 * What a formatting toolbar button asks for. Each carries the placeholder text inserted when
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

    /** A table with a header of [columnNames] and [rows] rows of [cellText], the first header selected to type over. */
    data class Table(
        val columnNames: List<String>,
        val rows: Int,
        val cellText: String
    ) : FormatAction

    /** A fenced code block in [language] (its name after the fence, empty for plain text). */
    data class CodeBlock(
        val placeholder: String,
        val language: String = ""
    ) : FormatAction

    data object Divider : FormatAction

    /**
     * An embedded file, [markdown] written as it is (`![[photo.png]]`) on a line of its own, in place of the selection.
     */
    data class Embed(val markdown: String) : FormatAction

    /**
     * A link to the note at [path] from the note at [source]: `[[target]]`, [target] being its name (or path when the
     * name is not unique), or a Markdown link when that name holds what ends a wiki link early (`C# notes`).
     */
    data class NoteLink(
        val target: String,
        val path: RelativePath,
        val source: RelativePath?
    ) : FormatAction

    data class WebLink(val placeholder: String) : FormatAction

    /** A `#tag` named [name], chosen in the tag picker, in place of the selection. */
    data class Tag(val name: String) : FormatAction
}
