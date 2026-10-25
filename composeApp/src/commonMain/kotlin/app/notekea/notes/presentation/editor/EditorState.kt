package app.notekea.notes.presentation.editor

import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.RelativePath

/**
 * State of the note editor (`ROADMAP.md` 1.5). The text itself is not here: it lives in the ViewModel's text buffer,
 * which the text field edits directly, so a keystroke does not copy the whole note. [notePath] is null when no note is
 * open. [loadError] means the note could not be read (retry opens it again); [saveError] is a failed save, retried by
 * the next edit or Save. [hasConflict] means the file changed outside Notekea while there were unsaved edits (Keep
 * mine / Load theirs); [isDeleted] means the file disappeared while open (Restore / Close). Autosave waits while
 * either is shown, so nothing is overwritten or lost without the user's answer. [titleError] is why a new title was
 * refused (invalid, hidden or taken name), shown under the title until it is edited again. [find] is Find in note
 * while its bar is open, and null when it is closed.
 */
data class EditorState(
    val notePath: RelativePath? = null,
    val isLoading: Boolean = false,
    val loadError: NotesError? = null,
    val saveStatus: SaveStatus = SaveStatus.NONE,
    val saveError: NotesError? = null,
    val hasConflict: Boolean = false,
    val isDeleted: Boolean = false,
    val titleError: NotesError? = null,
    val find: FindState? = null
)
