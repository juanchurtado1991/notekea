package app.notekea.notes.presentation.editor

import app.notekea.core.domain.time.CalendarDate
import app.notekea.notes.domain.FrontmatterContent
import app.notekea.notes.domain.NotesError
import app.notekea.notes.domain.OutlineHeading
import app.notekea.notes.domain.RelativePath

/**
 * State of the note editor (`ROADMAP.md` 1.5). The text itself is not here: it lives in the ViewModel's text buffer,
 * which the text field edits directly, so a keystroke does not copy the whole note. [notePath] is null when no note is
 * open. [loadError] means the note could not be read (retry opens it again); [saveError] is a failed save, retried by
 * the next edit or Save. [hasConflict] means the file changed outside Notekea while there were unsaved edits (Keep
 * mine / Load theirs); [isDeleted] means the file disappeared while open (Restore / Close). Autosave waits while
 * either is shown, so nothing is overwritten or lost without the user's answer. [titleError] is why a new title was
 * refused (invalid, hidden or taken name), shown under the title until it is edited again. [find] is Find in note
 * while its bar is open, and null when it is closed. [isReading] shows the note rendered (Read mode) instead of its
 * Markdown; notes open in the mode the user last chose. [attachments] are the files the note embeds, in text order
 * ([AttachmentView]), and [actionError] why attaching, opening or locating one failed, until it is dismissed.
 * [outline] is the note's headings in text order (`ROADMAP.md` 1.9). [properties] is the note's frontmatter read as
 * properties, shown open while [isPropertiesExpanded], and as its YAML ([propertiesSource], the lines between the
 * `---`) while [isSourceShown] (`ROADMAP.md` 1.14); [today] is
 * the day the date calendars mark, read when the note was last shown or saved.
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
    val attachments: List<AttachmentView> = emptyList(),
    val actionError: NotesError? = null,
    val outline: List<OutlineHeading> = emptyList(),
    val properties: FrontmatterContent = FrontmatterContent.None,
    val propertiesSource: String = "",
    val isPropertiesExpanded: Boolean = true,
    val isSourceShown: Boolean = false,
    val today: CalendarDate? = null
)
