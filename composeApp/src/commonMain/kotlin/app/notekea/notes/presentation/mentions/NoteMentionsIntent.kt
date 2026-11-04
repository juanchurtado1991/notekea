package app.notekea.notes.presentation.mentions

import app.notekea.core.presentation.FeedIntent
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.notes.domain.NoteMention
import app.notekea.core.notes.domain.RelativePath

sealed interface NoteMentionsIntent {
    /** The active pane shows [note] (none when no note is open). */
    data class OnNoteChange(val note: RelativePath?) : NoteMentionsIntent, FeedIntent

    /** The vault's notes changed: one added, removed, renamed or edited here or by another app. */
    data class OnNotesChange(val notes: Map<RelativePath, NoteEntry>) : NoteMentionsIntent, FeedIntent

    /** Link was clicked on an unlinked [mention]: its words become a link to the open note. */
    data class OnLink(val mention: NoteMention) : NoteMentionsIntent

    /** Try again after a search that failed. */
    data object OnRetry : NoteMentionsIntent
}
