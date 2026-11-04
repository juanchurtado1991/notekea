package app.notekea.notes.domain

/** Why an indexing run stopped before its end. */
sealed interface IndexRunError {
    /** The embedding model is missing or failed: nothing more can be indexed. */
    data object Model : IndexRunError

    /** The index is held by another Notekea or cannot be written. */
    data object Index : IndexRunError
}
