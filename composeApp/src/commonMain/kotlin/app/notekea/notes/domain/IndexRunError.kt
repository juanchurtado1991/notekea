package app.notekea.notes.domain

/** Why an indexing run stopped before its end. */
enum class IndexRunError {
    /** The embedding model is missing or failed: nothing more can be indexed. */
    MODEL,

    /** The index is held by another Notekea or cannot be written. */
    INDEX
}
