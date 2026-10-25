package app.notekea.notes.presentation.editor

import app.notekea.notes.domain.RelativePath

/** What the user (or the window around the editor) asked of the note editor. */
sealed interface EditorIntent {
    /** The deleted note is closed instead of restored. */
    data object OnCloseDeleted : EditorIntent

    /** Save now and report [EditorEffect.Flushed], before the window closes. */
    data object OnFlushRequest : EditorIntent

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
