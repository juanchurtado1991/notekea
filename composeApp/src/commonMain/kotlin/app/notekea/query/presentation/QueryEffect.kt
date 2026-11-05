package app.notekea.query.presentation

import app.notekea.core.notes.domain.RelativePath

/** One-shot events of a query. */
sealed interface QueryEffect {
    /** The note at [path] opens in a tab. */
    data class OpenNote(val path: RelativePath) : QueryEffect

    /** The note at [path] was written from a query: an editor that has it open takes the new text. */
    data class NoteRewritten(val path: RelativePath) : QueryEffect
}
