package app.notekea.notes.presentation.switcher

import app.notekea.notes.domain.RelativePath

/** One-shot events of the quick switcher. */
sealed interface QuickSwitcherEffect {
    /** A result was chosen: the workspace opens the note and selects it in the tree. */
    data class OpenNote(val path: RelativePath) : QuickSwitcherEffect
}
