package app.notekea.notes.presentation.editor

import app.notekea.notes.domain.Embed
import app.notekea.notes.domain.FormatAction
import app.notekea.notes.domain.ListAction
import app.notekea.notes.domain.RelativePath
import app.notekea.notes.domain.SidePanelTab

/** What the user (or the window around the editor) asked of the note editor. */
sealed interface EditorIntent {
    /** The deleted note is closed instead of restored. */
    data object OnCloseDeleted : EditorIntent

    /** Save now and report [EditorEffect.Flushed], before the window closes. */
    data object OnFlushRequest : EditorIntent

    /** A link to a note was clicked in Read mode; with `Mod` held its tab opens [inBackground] (`ROADMAP.md` 1.11). */
    data class OnLinkClick(
        val link: ReadLink.Note,
        val inBackground: Boolean = false
    ) : EditorIntent

    /**
     * Asks for the keyboard in the text of [path] (the note open now when null), as soon as it is open: after the
     * quick switcher closes, typing goes on in the note that was open or the one just chosen.
     */
    data class OnFocusRequest(val path: RelativePath? = null) : EditorIntent

    /** A task's checkbox was clicked in Edit mode: the task at [stateIndex] of the text is checked or unchecked. */
    data class OnTaskToggle(val stateIndex: Int) : EditorIntent

    /** `Mod+Enter`: the task on the cursor's line, if any, is checked or unchecked (Edit mode only). */
    data object OnTaskToggleShortcut : EditorIntent

    /** A formatting toolbar button or shortcut: [action] is applied to the selection (Edit mode only). */
    data class OnFormat(val action: FormatAction) : EditorIntent

    /** The toolbar's Link to a note (`Mod+K`): the window lets the person pick the note to link. */
    data object OnNoteLinkRequest : EditorIntent

    /** A `#tag` clicked in Read mode: it is searched in all notes. */
    data class OnTagClick(val tag: String) : EditorIntent

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

    /** Enter, Tab, Shift+Tab or Backspace in a list item or a quote, which edit the list (`ListEditing`). */
    data class OnListKey(val action: ListAction) : EditorIntent

    /** The toolbar's Tag button: choose a tag in the tag picker. */
    data object OnTagRequest : EditorIntent

    /** `Mod+T` or the toolbar's Insert template: the window opens the template picker, then sends a template. */
    data object OnTemplateRequest : EditorIntent

    /** The star beside the title: bookmarks the note, or removes its bookmark (`ROADMAP.md` 1.13). */
    data object OnBookmarkToggle : EditorIntent

    /** The [template] chosen in the picker: filled in and inserted at the cursor, or as the whole of an empty note. */
    data class OnTemplateInsert(val template: RelativePath) : EditorIntent

    /** Mod+V in the text: a copied image is attached and embedded at the cursor, copied text is pasted. */
    data object OnPaste : EditorIntent

    /** Files dropped on the text, by their paths on the computer: each is attached and embedded at the cursor. */
    data class OnFilesDrop(val paths: List<String>) : EditorIntent

    /** The toolbar's Image or file: a file chosen in the system's picker is attached and embedded at the cursor. */
    data object OnAttachmentPick : EditorIntent

    /** A file chip was clicked: open the file with the system's app. */
    data class OnAttachmentOpen(val path: RelativePath) : EditorIntent

    /** Locate… on a missing file: the file chosen in the picker takes the embed's place. */
    data class OnAttachmentLocate(val embed: Embed) : EditorIntent

    data object OnActionErrorDismiss : EditorIntent

    /** Mod+E or the header button: switch between Edit and Read mode. */
    data object OnReadModeToggle : EditorIntent

    /** Write the deleted note back with the editor's text. */
    data object OnRestore : EditorIntent

    data object OnRetryOpen : EditorIntent

    /** Mod+S: save at once instead of waiting for the autosave. */
    data object OnSaveShortcut : EditorIntent

    /** A heading of the outline was clicked or chosen with Enter: the editor goes to the [index]th heading. */
    data class OnHeadingClick(val index: Int) : EditorIntent

    /**
     * `Mod+Shift+O`: the outline takes the keyboard, showing the side panel first if it was hidden. The side panel
     * belongs to the tabs around the editor, which handle this and the next two.
     */
    data object OnOutlineFocusRequest : EditorIntent

    /** `Mod+Shift+\` or the panel's button: show or hide the panel on the right of the editor. */
    data object OnSidePanelToggle : EditorIntent

    /** The user finished dragging the side panel's edge to [widthDp]. */
    data class OnSidePanelResize(val widthDp: Int) : EditorIntent

    /** One of the side panel's tabs was chosen. */
    data class OnSidePanelTabSelect(val tab: SidePanelTab) : EditorIntent

    /**
     * "Show in side panel" on the note's properties, or "Show in the note" in the panel's Properties tab: the properties
     * move between the note and the panel (`ROADMAP.md` 1.14).
     */
    data object OnPropertiesPlacementToggle : EditorIntent

    /** The header's arrows of a daily note: the daily note before ([step] -1) or after ([step] 1) is opened. */
    data class OnDayStep(val step: Int) : EditorIntent

    /** The title field was confirmed with [name]: rename the note, and the links to it. */
    data class OnTitleCommit(val name: String) : EditorIntent

    /** The title is being edited again after a refused name. */
    data object OnTitleErrorDismiss : EditorIntent
}
