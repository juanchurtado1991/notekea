package app.notekea.search.presentation.index

import app.notekea.core.notes.domain.NoteFile
import app.notekea.core.presentation.FeedIntent

/** What the user did with the search index's status, details and notice, or what the vault's tree reports. */
sealed interface SearchIndexIntent {
    /** The vault's notes, sent whenever the tree changes; [isLoaded] once the first scan is done. */
    data class OnNotesChange(
        val notes: List<NoteFile>,
        val isLoaded: Boolean
    ) : SearchIndexIntent,
        FeedIntent

    /** The status bar's index status: opens or closes its details. */
    data object OnDetailsToggle : SearchIndexIntent

    data object OnDetailsDismiss : SearchIndexIntent

    /** The details' Rebuild index: forgets the index and reads every note again. */
    data object OnRebuildClick : SearchIndexIntent

    data object OnNoticeDismiss : SearchIndexIntent
}
