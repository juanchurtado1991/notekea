package app.notekea.notes.presentation.editor

import app.notekea.core.domain.markdown.CalloutKind
import app.notekea.notes.domain.FormatAction

/**
 * The toolbar's [FormatAction]s with their placeholders in the app's language, so the toolbar and the shortcuts insert
 * the same text. A callout is titled with its type in plain words ([calloutTitles]), as Read mode shows it; a table's
 * cells hold sample text ([tableCellText]) under numbered column names.
 */
data class FormatActions(
    val bold: FormatAction.Bold,
    val italic: FormatAction.Italic,
    val headings: List<FormatAction.Heading>,
    val webLink: FormatAction.WebLink,
    val bulletList: FormatAction.BulletList,
    val numberedList: FormatAction.NumberedList,
    val checklist: FormatAction.Checklist,
    val quote: FormatAction.Quote,
    val codePlaceholder: String,
    val tableColumnNames: List<String>,
    val tableCellText: String,
    val calloutPlaceholder: String,
    val calloutTitles: Map<CalloutKind, String>
) {
    /** A code block in [language], the name written after the fence (empty for plain text). */
    fun codeBlock(
        language: String
    ): FormatAction.CodeBlock = FormatAction.CodeBlock(placeholder = codePlaceholder, language = language)

    /** A table of [columns] (up to [TABLE_MAX_COLUMNS]) and [rows] rows of sample text to type over. */
    fun table(
        columns: Int,
        rows: Int
    ): FormatAction.Table = FormatAction.Table(
        columnNames = tableColumnNames.take(n = columns),
        rows = rows,
        cellText = tableCellText
    )

    fun callout(
        kind: CalloutKind,
        isFolded: Boolean
    ): FormatAction.Callout = FormatAction.Callout(
        type = kind.name.lowercase(),
        title = calloutTitles.getValue(kind),
        placeholder = calloutPlaceholder,
        isFolded = isFolded
    )

    companion object {
        /** The largest table the toolbar's size picker offers. */
        const val TABLE_MAX_COLUMNS = 8
        const val TABLE_MAX_ROWS = 6
    }
}
