package app.notekea.core.documents.domain

/**
 * A part of a document's text as the viewer and citations name it: a page of a PDF, a slide, a sheet, or a heading;
 * [number] counts from 1 among parts of its [kind], and [range] is where it lies in the text.
 */
data class DocumentPart(
    val kind: DocumentPartKind,
    val number: Int,
    val title: String,
    val range: IntRange
)
