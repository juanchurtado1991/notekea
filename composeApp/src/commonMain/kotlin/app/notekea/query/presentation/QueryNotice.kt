package app.notekea.query.presentation

import app.notekea.core.notes.domain.RelativePath

/** What the last write of a query told the person. */
sealed interface QueryNotice {
    /** The query is now a block at the end of the note at [path]. */
    data class Saved(val path: RelativePath) : QueryNotice

    /** The note at [path] already holds the query, so nothing was written. */
    data class AlreadySaved(val path: RelativePath) : QueryNotice

    /** The note could not be written, as one that changed meanwhile: nothing was. */
    data object WriteFailed : QueryNotice
}
