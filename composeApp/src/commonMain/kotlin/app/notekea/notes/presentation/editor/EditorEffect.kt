package app.notekea.notes.presentation.editor

import app.notekea.notes.domain.LinkKind
import app.notekea.notes.domain.LinkUpdateResult
import app.notekea.notes.domain.RelativePath

/** One-shot events of the note editor for the window around it. */
sealed interface EditorEffect {
    /** The user closed a note that was deleted outside Notekea, so nothing is selected anymore. */
    data object Closed : EditorEffect

    /** Everything pending was saved (or could not be), so the window may close. */
    data object Flushed : EditorEffect

    /** A link to [target] was clicked in [source]: the panes around the editor open the note it leads to. */
    data class OpenLink(
        val source: RelativePath,
        val target: String,
        val kind: LinkKind
    ) : EditorEffect

    /** The title renamed the note from [from] to [to]; [links] is how the links to it were updated, if any. */
    data class Renamed(
        val from: RelativePath,
        val to: RelativePath,
        val links: LinkUpdateResult?
    ) : EditorEffect
}
