package app.notekea.search.presentation.switcher

import app.notekea.core.notes.domain.RelativePath

/** One-shot events of the quick switcher. */
sealed interface QuickSwitcherEffect {
    /**
     * A result was chosen: the workspace opens the note, at its [passage] if it has one, and selects it in the tree.
     */
    data class OpenNote(
        val path: RelativePath,
        val passage: IntRange? = null
    ) : QuickSwitcherEffect

    /** A note was chosen to be linked from the open note (the toolbar's Link to a note). */
    data class LinkNote(val path: RelativePath) : QuickSwitcherEffect

    /** Closed without choosing (Esc or a click outside): whatever had the keyboard before gets it back. */
    data object Dismissed : QuickSwitcherEffect
}
