package app.notekea.notes.presentation.workspace

import androidx.compose.runtime.Immutable

/**
 * What an editor without tabs offers, and the "+" after the tabs: find a note ([onFindNote], also the "+"), a new note
 * ([onNewNote]), today's daily note ([onToday]) and a search in the notes' text ([onSearchNotes]).
 */
@Immutable
data class WorkspaceStartActions(
    val onFindNote: () -> Unit = {},
    val onNewNote: () -> Unit = {},
    val onToday: () -> Unit = {},
    val onSearchNotes: () -> Unit = {}
)
