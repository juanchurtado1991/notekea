package app.notekea.connections.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import app.notekea.core.notes.domain.NoteEntry
import app.notekea.core.notes.domain.RelativePath

/** The Linked and Unlinked tabs follow the vault: the active pane's [note] and the vault's [notes]. */
@Composable
fun FollowNoteMentions(
    notes: Map<RelativePath, NoteEntry>,
    onIntent: (NoteMentionsIntent) -> Unit,
    note: RelativePath?
) {
    LaunchedEffect(notes) { onIntent(NoteMentionsIntent.OnNotesChange(notes = notes)) }

    LaunchedEffect(note) { onIntent(NoteMentionsIntent.OnNoteChange(note = note)) }
}
