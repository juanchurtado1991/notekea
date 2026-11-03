package app.notekea.notes.presentation.related

import app.notekea.notes.domain.IndexRunError
import app.notekea.notes.domain.RelatedNote
import app.notekea.notes.domain.RelativePath

/**
 * The open note's Related section (`ROADMAP.md` 2.4): its [related] notes and those it [linked] already (folded unless
 * [isLinkedOpen]), while [isLoading] the first time for a note; [isIndexReady] once the index has run, so an empty list
 * before says the notes appear after indexing; the [error] that stopped the search.
 */
data class RelatedNotesState(
    val note: RelativePath? = null,
    val related: List<RelatedNote> = emptyList(),
    val linked: List<RelativePath> = emptyList(),
    val isLoading: Boolean = false,
    val isIndexReady: Boolean = false,
    val error: IndexRunError? = null,
    val isLinkedOpen: Boolean = false
)
