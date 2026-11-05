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

    /** The graph's tab came to be, or stopped being, open in a pane: it works only then. */
    data class OnShownChange(val isShown: Boolean) :
        GraphIntent,
        FeedIntent

    /** The graph was asked for from [note], or with no note open. */
    data class OnOpen(val note: RelativePath?) : GraphIntent

    data class OnScopeSelect(val scope: GraphScope) : GraphIntent

    data class OnDepthSelect(val depth: Int) : GraphIntent

    /** A click on a dot, or an arrow key: around a note, it moves to the center. */
    data class OnNoteSelect(val path: RelativePath) : GraphIntent

    data class OnMove(val direction: GraphDirection) : GraphIntent

    /** `Enter` or Open. */
    data object OnOpenSelected : GraphIntent

    /** A double click on the note at [path], which a first click did not select. */
    data class OnNoteOpen(val path: RelativePath) : GraphIntent

    /** `Esc`: back to the note the graph was opened from. */
    data object OnBack : GraphIntent

    /** `Space`. */
    data object OnInspectorToggle : GraphIntent

    data object OnRelatedToggle : GraphIntent

    data object OnOrphansToggle : GraphIntent

    /** `Mod+F` or the search button. */
    data object OnSearchOpen : GraphIntent

    data object OnSearchClose : GraphIntent

    data class OnQueryChange(val query: String) : GraphIntent

    /** The next match, or the one before when [isForward] is false: the map moves to its graph. */
    data class OnMatchStep(val isForward: Boolean) : GraphIntent

    /** Link, in the inspector: the note it shows gets a link to [path]. */
    data class OnLink(val path: RelativePath) : GraphIntent

    data object OnLinkUndo : GraphIntent

    /** A folder of the legend, left out or brought back. */
    data class OnFolderToggle(val folder: String) : GraphIntent
}
