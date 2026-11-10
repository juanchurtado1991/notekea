package app.notekea.editor.presentation.model

import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.SidePanelMode
import app.notekea.core.notes.domain.SidePanelSection
import app.notekea.core.presentation.FeedIntent
import app.notekea.editor.domain.Embed
import app.notekea.editor.domain.FormatAction
import app.notekea.editor.domain.ListAction
import app.notekea.editor.domain.TableAction
import app.notekea.editor.presentation.reading.ReadLink
import kotlinx.coroutines.CompletableDeferred

/** What the user (or the window around the editor) asked of the note editor. */
sealed interface EditorIntent {
    /** The deleted note is closed instead of restored. */
    data object OnCloseDeleted : EditorIntent

    /** Another pane is about to open this note: [done] completes once this editor has saved what it holds. */
    data class OnHandOver(val done: CompletableDeferred<Unit>) : EditorIntent

    /** Save now and report [EditorEffect.Flushed], before the window closes. */
    data object OnFlushRequest :
        EditorIntent,
        FeedIntent

    /** A link to a note was clicked; with `Mod` held its tab opens [inBackground]. */
    data class OnLinkClick(
        val link: ReadLink.Note,
        val inBackground: Boolean = false
    ) : EditorIntent

    /**
     * Asks for the keyboard in the text of [path] (the note open now when null), as soon as it is open: after the
     * quick switcher closes, typing goes on in the note that was open or the one just chosen.
     */
    data class OnFocusRequest(val path: RelativePath? = null) : EditorIntent

    /** A task's checkbox was clicked: the task at [stateIndex] of the text is checked or unchecked. */
    data class OnTaskToggle(val stateIndex: Int) : EditorIntent

    /** `Mod+Enter`: the task on the cursor's line, if any, is checked or unchecked. */
    data object OnTaskToggleShortcut : EditorIntent

    /** A formatting toolbar button or shortcut: [action] is applied to the selection. */
    data class OnFormat(val action: FormatAction) : EditorIntent

    /** The toolbar's Link to a note (`Mod+K`): the window lets the person pick the note to link. */
    data object OnNoteLinkRequest : EditorIntent

    /** A `#tag` clicked: it is searched in all notes. */
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

    /** A rename's link update rewrote the open note at [path]. */
    data class OnNoteRewritten(val path: RelativePath) : EditorIntent

    /** Open [path], saving the current note first; null closes the editor. */
    data class OnOpen(val path: RelativePath?) : EditorIntent

    /** Enter, Tab, Shift+Tab or Backspace in a list item or a quote, which edit the list (`ListEditing`). */
    data class OnListKey(val action: ListAction) : EditorIntent

    /** A table changed from the cursor's cell: its bar, its menu, or a key that needs a new row (`TableEditing`). */
    data class OnTableEdit(val action: TableAction) : EditorIntent

    /** The toolbar's Tag button: choose a tag in the tag picker. */
    data object OnTagRequest : EditorIntent

    /** `Mod+T` or the toolbar's Insert template: the window opens the template picker, then sends a template. */
    data object OnTemplateRequest : EditorIntent

    /** The star beside the title: bookmarks the note, or removes its bookmark. */
    data object OnBookmarkToggle : EditorIntent

    /** The note's folder above its title was clicked: the window shows that folder in the tree. */
    data object OnFolderReveal : EditorIntent

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

    /** The toolbar's Source button or Mod+E: show the whole note as written, or as it looks again. */
    data object OnSourceModeToggle : EditorIntent

    /** Write the deleted note back with the editor's text. */
    data object OnRestore : EditorIntent

    data object OnRetryOpen : EditorIntent

    /** Mod+S: save at once instead of waiting for the autosave. */
    data object OnSaveShortcut : EditorIntent

    /** A heading of the outline was clicked or chosen with Enter: the editor goes to the [index]th heading. */
    data class OnHeadingClick(val index: Int) : EditorIntent

    /** A mention was chosen in Connections: [range] of [path]'s body is selected once it is open. */
    data class OnRevealRequest(
        val path: RelativePath,
        val range: IntRange
    ) : EditorIntent

    /**
     * `Mod+Shift+O`: the outline takes the keyboard, showing the side panel and opening the outline first if they were
     * not. The side panel belongs to the tabs around the editor, which handle this and the next four.
     */
    data object OnOutlineFocusRequest : EditorIntent

    /** `Mod+Shift+\` or the panel's button: show or hide the panel on the right of the editor. */
    data object OnSidePanelToggle : EditorIntent

    /** The user finished dragging the side panel's edge to [widthDp]. */
    data class OnSidePanelResize(val widthDp: Int) : EditorIntent

    /** The side panel's Inspector | Chat control: it shows [mode]. */
    data class OnSidePanelModeSelect(val mode: SidePanelMode) : EditorIntent

    /** A side panel section's header: the section folds or opens. */
    data class OnSidePanelSectionToggle(val section: SidePanelSection) : EditorIntent

    /** The header's arrows of a daily note: the daily note before ([step] -1) or after ([step] 1) is opened. */
    data class OnDayStep(val step: Int) : EditorIntent

    /** The title field was confirmed with [name]: rename the note, and the links to it. */
    data class OnTitleCommit(val name: String) : EditorIntent

    /** The title is being edited again after a refused name. */
    data object OnTitleErrorDismiss : EditorIntent
}
