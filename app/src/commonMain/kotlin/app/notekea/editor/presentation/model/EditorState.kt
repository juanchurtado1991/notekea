package app.notekea.editor.presentation.model

import app.notekea.core.domain.time.CalendarDate
import app.notekea.core.notes.domain.FrontmatterContent
import app.notekea.core.notes.domain.NotesError
import app.notekea.core.notes.domain.OutlineHeading
import app.notekea.core.notes.domain.RelativePath
import app.notekea.editor.presentation.attachments.AttachmentView
import app.notekea.editor.presentation.find.FindState
import app.notekea.editor.presentation.noteinfo.NoteInfo

/**
 * State of the note editor. The text is not here: it lives in the ViewModel's text buffer, which the text field edits
 * directly, so a keystroke does not copy the whole note. Autosave waits while [hasConflict] or [isDeleted] is shown, so
 * nothing is overwritten without the user's answer. [bodyStartLine] counts the frontmatter's lines.
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
    val find: FindState? = null,
    val isReading: Boolean = false,
    val isRevealing: Boolean = false,
    val attachments: List<AttachmentView> = emptyList(),
    val actionError: NotesError? = null,
    val outline: List<OutlineHeading> = emptyList(),
    val properties: FrontmatterContent = FrontmatterContent.None,
    val propertiesSource: String = "",
    val isPropertiesExpanded: Boolean = true,
    val isSourceShown: Boolean = false,
    val today: CalendarDate? = null,
    val info: NoteInfo = NoteInfo(),
    val usesCrLf: Boolean = false,
    val bodyStartLine: Int = 0
) {
    /** Whether the text can be changed now: a note is open and loaded, in Edit mode. */
    val isEditable: Boolean get() =
        notePath != null &&
            !isLoading &&
            !isReading
}
