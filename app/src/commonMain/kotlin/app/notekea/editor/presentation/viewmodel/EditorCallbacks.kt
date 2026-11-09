package app.notekea.editor.presentation.viewmodel

import androidx.compose.runtime.Immutable
import app.notekea.core.notes.domain.RelativePath
import app.notekea.editor.presentation.model.EditorEffect

/**
 * What the editor of a pane asks of the panes around it: a rename, a deleted note closed, a link or folder clicked, a
 * tag searched, a picker for a note link, a tag or a template, the star toggled, a daily note's arrow, or unsaved text
 * that was kept. [onBeforeOpen] runs, and is waited for, before it opens a note.
 */
@Immutable
data class EditorCallbacks(
    val onRenamed: (EditorEffect.Renamed) -> Unit,
    val onClosed: () -> Unit,
    val onOpenLink: (EditorEffect.OpenLink) -> Unit,
    val onSearchTag: (String) -> Unit,
    val onRequestNoteLink: () -> Unit,
    val onRequestTag: (String) -> Unit,
    val onRequestTemplate: () -> Unit,
    val onBookmarkToggle: (RelativePath) -> Unit,
    val onDayStep: (Int) -> Unit,
    val onRevealFolder: (RelativePath) -> Unit,
    val onUnsavedKept: (EditorEffect.UnsavedKept) -> Unit,
    val onBeforeOpen: suspend (RelativePath) -> Unit
)
