package app.notekea.core.documents.domain

import app.notekea.core.notes.domain.RelativePath

/**
 * The documents Notekea reads (4.5), by extension: indexed like notes and shown in a read-only tab, never edited.
 * [isPaged] documents (PDF) show their real pages; the others show their text.
 */
enum class DocumentFormat(
    val extensions: Set<String>,
    val isPaged: Boolean = false
) {
    PDF(
        extensions = setOf("pdf"),
        isPaged = true
    ),
    WORD(extensions = setOf("docx")),
    OPEN_DOCUMENT(extensions = setOf("odt")),
    EPUB(extensions = setOf("epub")),
    POWERPOINT(extensions = setOf("pptx")),
    EXCEL(extensions = setOf("xlsx")),
    HTML(extensions = setOf("html", "htm")),

    /** Plain text read as it is: no library needed. */
    TEXT(extensions = setOf("txt", "rst", "org", "csv", "json"));

    companion object {
        fun of(path: RelativePath): DocumentFormat? {
            val extension = path
                .name
                .substringAfterLast(
                    delimiter = '.',
                    missingDelimiterValue = ""
                )
                .lowercase()

            return entries.firstOrNull {
                extension in it.extensions
            }
        }
    }
}
