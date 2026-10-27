package app.notekea.notes.presentation.editor

import app.notekea.notes.domain.LinkKind
import app.notekea.notes.domain.LinkUpdateResult
import app.notekea.notes.domain.OutlineHeading
import app.notekea.notes.domain.RelativePath

/** One-shot events of the note editor for the window around it. */
sealed interface EditorEffect {
    /** The user closed a note that was deleted outside Notekea, so nothing is selected anymore. */
    data object Closed : EditorEffect

    /** Everything pending was saved (or could not be), so the window may close. */
    data object Flushed : EditorEffect

    /**
     * A link to [target] was clicked in [source]: the panes around the editor open the note it leads to, in a tab behind
     * the current one when [inBackground].
     */
    data class OpenLink(
        val source: RelativePath,
        val target: String,
        val kind: LinkKind,
        val inBackground: Boolean = false
    ) : EditorEffect

    /** The text of the open note should take the keyboard (in Edit mode; Read mode has no cursor). */
    data object FocusText : EditorEffect

    /** A daily note's arrow was clicked: the window opens the daily note before ([step] -1) or after ([step] 1). */
    data class DayStep(val step: Int) : EditorEffect

    /** An outline [heading] was chosen: the editor scrolls so it sits at the top of the note's view. */
    data class RevealHeading(val heading: OutlineHeading) : EditorEffect

    /** Link to a note was asked for: the window opens the palette to pick it, then sends a note link to format. */
    data object RequestNoteLink : EditorEffect

    /** Tag was asked for: the window opens the tag picker with [query] (the selected text) typed, then sends a tag. */
    data class RequestTag(val query: String) : EditorEffect

    /** The star beside the title was clicked: the window bookmarks the note at [path], or removes its bookmark. */
    data class ToggleBookmark(val path: RelativePath) : EditorEffect

    /** The note's folder was clicked: the window shows the folder at [path] in the tree, selected. */
    data class RevealFolder(val path: RelativePath) : EditorEffect

    /** Insert template was asked for: the window opens the template picker, then sends the template to insert. */
    data object RequestTemplate : EditorEffect

    /** A `#tag` was clicked in Read mode: the window searches it in all notes. */
    data class SearchTag(val tag: String) : EditorEffect

    /** The title renamed the note from [from] to [to]; [links] is how the links to it were updated, if any. */
    data class Renamed(
        val from: RelativePath,
        val to: RelativePath,
        val links: LinkUpdateResult?
    ) : EditorEffect
}
