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

    /** The description named [property], which no note has; [suggestions] are the closest the vault has. */
    data class UnknownProperty(
        val property: String,
        val suggestions: List<String>
    ) : QueryNotice

    /** The description could not be turned into a query (no model, or none written). */
    data object NotUnderstood : QueryNotice
}
