package app.notekea.query.presentation

import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.NoteProperty
import app.notekea.core.notes.domain.RelativePath
import app.notekea.query.domain.QueryFilter
import app.notekea.query.domain.QuerySort

/** What can happen to a query: the vault changes, its tab shows, its controls are used, or its table is edited. */
sealed interface QueryIntent {
    data class OnNotesChange(val notes: Collection<NoteEntry>) : QueryIntent

    data class OnShownChange(val isShown: Boolean) : QueryIntent

    data class OnFilterAdd(val filter: QueryFilter) : QueryIntent

    data class OnFilterChange(val index: Int, val filter: QueryFilter) : QueryIntent

    data class OnFilterRemove(val index: Int) : QueryIntent

    data class OnSortChange(val sort: QuerySort?) : QueryIntent

    data class OnColumnToggle(val column: String) : QueryIntent

    data class OnNoteOpen(val path: RelativePath) : QueryIntent

    data class OnCellChange(val path: RelativePath, val property: NoteProperty) : QueryIntent

    data class OnSave(val path: RelativePath) : QueryIntent

    data object OnNoticeDismiss : QueryIntent
}
