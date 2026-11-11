package app.notekea.core.documents.data

import app.notekea.core.documents.domain.DocumentFormat
import app.notekea.core.documents.domain.DocumentPart
import app.notekea.core.documents.domain.DocumentPartKind
import app.notekea.core.documents.domain.DocumentText
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler

/**
 * Tika's XHTML of a document as Markdown: headings, paragraphs, list items and tables as a person would write them,
 * a `## Page n` before each page of a PDF and `## Slide n` before each slide, `## Sheet` and its name before each
 * sheet of a workbook, and the [DocumentPart] of each, so a citation knows the page it is on.
 */
internal class MarkdownContentHandler(private val format: DocumentFormat) : DefaultHandler() {
    private val text = StringBuilder()
    private val line = StringBuilder()
    private val parts = mutableListOf<DocumentPart>()
    private val row = mutableListOf<String>()
    private var heading = 0
    private var isListItem = false
    private var rowsInTable = 0
    private var pages = 0
    private var slides = 0
    private var sheets = 0
    private var isSheetTitle = false
    private var isInHead = false

    fun result(): DocumentText {
        flush()
        closeLastPart()

        return DocumentText(
            text = text.toString().trimEnd() + "\n",
            parts = parts.toList()
        )
    }

    override fun startElement(
        uri: String?,
        localName: String?,
        qName: String?,
        attributes: Attributes?
    ) {
        val name = (localName ?: qName).orEmpty().lowercase()
        val css = attributes?.getValue(CLASS).orEmpty()

        when {
            name == DIV &&
                css == PAGE &&
                format == DocumentFormat.PDF -> part(kind = DocumentPartKind.PAGE)

            name == DIV && css == SLIDE -> part(kind = DocumentPartKind.SLIDE)

            name == DIV &&
                (css == SHEET || css == PAGE) &&
                format == DocumentFormat.EXCEL -> isSheetTitle = true

            name == HEAD -> isInHead = true

            name.length == 2 &&
                name[0] == 'h' &&
                name[1].isDigit() -> start(level = name[1].digitToInt())

            name == LIST_ITEM -> startItem()

            name == TABLE -> rowsInTable = 0

            name == ROW -> row.clear()

            name == CELL || name == HEAD_CELL -> line.clear()

            name in BLOCKS -> flush()
        }
    }

    override fun endElement(
        uri: String?,
        localName: String?,
        qName: String?
    ) {
        val name = (localName ?: qName).orEmpty().lowercase()

        when {
            name == HEAD -> isInHead = false

            name.length == 2 &&
                name[0] == 'h' &&
                name[1].isDigit() -> endHeading()

            name == CELL || name == HEAD_CELL ->
                row += line
                    .toString()
                    .trim()
                    .replace(
                        oldValue = "|",
                        newValue = "\\|"
                    )
                    .also { line.clear() }

            name == ROW -> endRow()

            name == LIST_ITEM -> {
                flush()
                isListItem = false
            }

            name in BLOCKS -> flush()
        }
    }

    override fun characters(
        ch: CharArray,
        start: Int,
        length: Int
    ) {
        if (isInHead) return
        val piece = String(ch, start, length).replace(WHITESPACE, " ")
        val isSpaceAgain = piece.isBlank() && (line.isEmpty() || line.endsWith(' '))
        if (isSpaceAgain) return
        line.append(piece)
    }

    private fun start(level: Int) {
        flush()

        heading = level.coerceIn(
            minimumValue = 1,
            maximumValue = MAX_HEADING
        )
    }

    private fun endHeading() {
        val title = line.toString().trim()
        line.clear()

        if (isSheetTitle) {
            isSheetTitle = false

            part(
                kind = DocumentPartKind.SHEET,
                title = title
            )
        } else if (title.isNotEmpty()) {
            // A document's own headings sit under the page or slide headings.
            val level = if (parts.isEmpty()) heading else heading + 2
            paragraph(value = "#".repeat(n = level.coerceAtMost(maximumValue = MAX_HEADING)) + " " + title)
        }

        heading = 0
    }

    private fun startItem() {
        flush()
        isListItem = true
    }

    private fun endRow() {
        val isEmptyRow = row.isEmpty() || row.all { it.isEmpty() }
        if (isEmptyRow) return

        text
            .append("| ")
            .append(row.joinToString(separator = " | "))
            .append(" |\n")

        if (rowsInTable == 0) {
            text
                .append("|")
                .append(" --- |".repeat(n = row.size))
                .append('\n')
        }

        rowsInTable++
        row.clear()
    }

    /** Starts a new page, slide or sheet: the last one ends here. */
    private fun part(
        kind: DocumentPartKind,
        title: String? = null
    ) {
        flush()
        closeLastPart()

        val number = when (kind) {
            DocumentPartKind.PAGE -> ++pages
            DocumentPartKind.SLIDE -> ++slides
            DocumentPartKind.SHEET -> ++sheets
            DocumentPartKind.SECTION -> parts.size + 1
        }

        val name = title ?: "${LABELS.getValue(kind)} $number"

        val heading = when (kind) {
            DocumentPartKind.SHEET -> "## ${LABELS.getValue(kind)}: $name"
            else -> "## $name"
        }

        val needsBreak = text.isNotEmpty() && !text.endsWith("\n\n")
        if (needsBreak) text.append('\n')
        val start = text.length
        text.append(heading).append("\n\n")

        parts += DocumentPart(
            kind = kind,
            number = number,
            title = name,
            range = start until start
        )
    }

    private fun closeLastPart() {
        val last = parts.lastOrNull() ?: return
        parts[parts.lastIndex] = last.copy(range = last.range.first until text.length)
    }

    private fun flush() {
        val value = line.toString().trim()
        line.clear()
        if (value.isEmpty()) return
        val isAfterTable = rowsInTable > 0 && !text.endsWith("\n\n")
        if (isAfterTable) text.append('\n')
        rowsInTable = 0

        paragraph(value = if (isListItem) "- $value" else value)
    }

    private fun paragraph(value: String) {
        text.append(value).append(if (isListItem) "\n" else "\n\n")
    }

    private companion object {
        const val CLASS = "class"
        const val DIV = "div"
        const val PAGE = "page"
        const val SLIDE = "slide-content"
        const val SHEET = "sheet"
        const val HEAD = "head"
        const val LIST_ITEM = "li"
        const val TABLE = "table"
        const val ROW = "tr"
        const val CELL = "td"
        const val HEAD_CELL = "th"
        const val MAX_HEADING = 6
        val BLOCKS =
            setOf("p", "div", "blockquote", "pre", "br", "ul", "ol", "table", "section", "article")
        val WHITESPACE = Regex(pattern = "\\s+")
        val LABELS = mapOf(
            DocumentPartKind.PAGE to "Page",
            DocumentPartKind.SLIDE to "Slide",
            DocumentPartKind.SHEET to "Sheet",
            DocumentPartKind.SECTION to "Section"
        )
    }
}
