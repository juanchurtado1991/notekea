package app.notekea.core.documents.domain

/**
 * What a document says, as Markdown the index chunks and the chat cites like a note's: headings, paragraphs, lists
 * and tables, with a heading for each page, slide or sheet; [parts] say where each lies.
 */
data class DocumentText(
    val text: String,
    val parts: List<DocumentPart> = emptyList()
) {
    /** The part [offset] lies in: the page, slide or sheet a citation of it opens at. */
    fun partAt(offset: Int): DocumentPart? = parts.lastOrNull {
        it.range.first <= offset
    }
}
