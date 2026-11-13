package app.notekea.connections.presentation

import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.index.IndexRunError
import app.notekea.core.notes.domain.index.RelatedNote

/**
 * The open note's Related section: its [related] notes and those it [linked] already (folded unless [isLinkedOpen]),
 * while [isLoading] the first time; [isIndexReady] once the index has run; the [error] that stopped the search;
 * [isTooLong] when the model could read none of the note.
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
