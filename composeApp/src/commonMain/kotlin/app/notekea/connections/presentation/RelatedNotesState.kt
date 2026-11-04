package app.notekea.connections.presentation

import app.notekea.core.notes.domain.IndexRunError
import app.notekea.connections.domain.RelatedNote
import app.notekea.core.notes.domain.RelativePath

/**
 * The open note's Related section (`ROADMAP.md` 2.4): its [related] notes and those it [linked] already (folded unless
 * [isLinkedOpen]), while [isLoading] the first time for a note; [isIndexReady] once the index has run, so an empty list
 * before says the notes appear after indexing; the [error] that stopped the search; [isTooLong] when the model could
 * read none of the note.
 */
data class RelatedNotesState(
    val note: RelativePath? = null,
    val related: List<RelatedNote> = emptyList(),
    val linked: List<RelativePath> = emptyList(),
    val isLoading: Boolean = false,
    val isIndexReady: Boolean = false,
    val error: IndexRunError? = null,
    val isLinkedOpen: Boolean = false,
    val isTooLong: Boolean = false
)
