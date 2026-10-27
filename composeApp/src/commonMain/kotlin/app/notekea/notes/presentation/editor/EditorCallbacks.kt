package app.notekea.notes.presentation.editor

import androidx.compose.runtime.Immutable

/**
 * What the editor of a pane asks of the panes around it: the title renamed the note ([onRenamed]), a deleted note was
 * closed ([onClosed]), a link was clicked ([onOpenLink]), a `#tag` is searched ([onSearchTag]), a note to link or a
 * tag is picked ([onRequestNoteLink], [onRequestTag]), a template is picked ([onRequestTemplate]), or a daily note's
 * arrow went to another day ([onDayStep]).
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
    val onDayStep: (Int) -> Unit
)
