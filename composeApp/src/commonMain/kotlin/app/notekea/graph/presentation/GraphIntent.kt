package app.notekea.graph.presentation

import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.FeedIntent

/** What the user did on the graph, plus what it follows: the vault's notes and its index. */
sealed interface GraphIntent {
    data class OnNotesChange(val notes: Collection<NoteEntry>) :
        GraphIntent,
        FeedIntent

    data class OnIndexChange(val isReady: Boolean) :
        GraphIntent,
        FeedIntent

    /** The graph was asked for from [note], or with no note open. */
    data class OnOpen(val note: RelativePath?) : GraphIntent

    data class OnScopeSelect(val scope: GraphScope) : GraphIntent

    data class OnDepthSelect(val depth: Int) : GraphIntent

    /** A click on a dot, or an arrow key: around a note, it moves to the center. */
    data class OnNoteSelect(val path: RelativePath) : GraphIntent

    data class OnMove(val direction: GraphDirection) : GraphIntent

    /** `Enter`, a double click or Open. */
    data object OnOpenSelected : GraphIntent

    /** `Esc`: back to the note the graph was opened from. */
    data object OnBack : GraphIntent

    /** `Space`. */
    data object OnInspectorToggle : GraphIntent

    data object OnRelatedToggle : GraphIntent

    data object OnOrphansToggle : GraphIntent

    /** A folder of the legend, left out or brought back. */
    data class OnFolderToggle(val folder: String) : GraphIntent
}
