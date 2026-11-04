package app.notekea.notes.presentation.related

import app.notekea.core.presentation.FeedIntent
import app.notekea.core.notes.domain.RelativePath

/** What the Related section is told: the open note and its text, the vault's notes, the index's runs, and a fold. */
sealed interface RelatedNotesIntent {
    /** The active pane's note ([note], null when none) and its [text] as the editor has it, at every edit. */
    data class OnNoteChange(
        val text: String,
        val note: RelativePath?
    ) : RelatedNotesIntent,
        FeedIntent

    /** The vault's notes, for the links of the open note. */
    data class OnNotesChange(val notes: List<RelativePath>) :
        RelatedNotesIntent,
        FeedIntent

    /** The index ended a run ([isReady]): what is related may have changed. */
    data class OnIndexChange(val isReady: Boolean) :
        RelatedNotesIntent,
        FeedIntent

    /** "Already linked" opened or folded. */
    data object OnLinkedToggle : RelatedNotesIntent
}
