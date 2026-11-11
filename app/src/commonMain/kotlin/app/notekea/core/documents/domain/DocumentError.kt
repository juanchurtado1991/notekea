package app.notekea.core.documents.domain

/** Why a document could not be read: it is only skipped and named, never a reason to stop indexing the others. */
sealed interface DocumentError {
    /** Damaged, locked with a password, or not what its extension says. */
    data class Unreadable(val message: String) : DocumentError

    /** Pages with no text, as a scanned PDF: reading them needs OCR, which comes after the MVP. */
    data object NoText : DocumentError

    data object Missing : DocumentError
}
