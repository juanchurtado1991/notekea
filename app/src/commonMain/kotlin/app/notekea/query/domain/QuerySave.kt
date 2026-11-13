package app.notekea.query.domain

import app.notekea.core.notes.domain.links.NoteRewrite

/** What saving a query in a note came to. */
sealed interface QuerySave {
    data class Written(val rewrite: NoteRewrite) : QuerySave

    /** The note already holds this query, so nothing was written. */
    data object AlreadyThere : QuerySave

    /** The note could not be read or written, as one that changed meanwhile. */
    data object Failed : QuerySave
}
