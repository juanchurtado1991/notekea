package app.notekea.notes.presentation.editor

import app.notekea.notes.domain.RelativePath

/** What the user (or the window around the editor) asked of the note editor. */
sealed interface EditorIntent {
    /** The deleted note is closed instead of restored. */
    data object OnCloseDeleted : EditorIntent

    /** Save now and report [EditorEffect.Flushed], before the window closes. */
    data object OnFlushRequest : EditorIntent

    /** Esc in the find bar: close it, leaving the current match selected in the text. */
    data object OnFindClose : EditorIntent

    /** Enter in the find bar, or its down arrow: the next match, after the last one the first. */
    data object OnFindNext : EditorIntent

    /** Mod+F: open the find bar (it keeps the last query while the note stays open). */
    data object OnFindOpen : EditorIntent

    /** Shift+Enter in the find bar, or its up arrow: the previous match, before the first one the last. */
    data object OnFindPrevious : EditorIntent

    /** The text searched for changed. */
    data class OnFindQueryChange(val query: String) : EditorIntent

    /** The text left the keyboard focus (the user clicked the tree, for instance): save now. */
    data object OnFocusLost : EditorIntent

    /** Overwrite the outside change with the editor's text. */
    data object OnKeepMine : EditorIntent

    /** Drop the unsaved edits and show the text on disk. */
    data object OnLoadTheirs : EditorIntent

    /** The open note was renamed or moved in Notekea and now lives at [path]; nothing is saved to the old place. */
    data class OnNoteMoved(val path: RelativePath) : EditorIntent

    /** Open [path], saving the current note first; null closes the editor. */
    data class OnOpen(val path: RelativePath?) : EditorIntent

    /** Mod+E or the header button: switch between Edit and Read mode. */
    data object OnReadModeToggle : EditorIntent

    /** Write the deleted note back with the editor's text. */
    data object OnRestore : EditorIntent

    data object OnRetryOpen : EditorIntent

    /** Mod+S: save at once instead of waiting for the autosave. */
    data object OnSaveShortcut : EditorIntent

    /** The title field was confirmed with [name]: rename the note, and the links to it. */
    data class OnTitleCommit(val name: String) : EditorIntent

    /** The title is being edited again after a refused name. */
    data object OnTitleErrorDismiss : EditorIntent
}
