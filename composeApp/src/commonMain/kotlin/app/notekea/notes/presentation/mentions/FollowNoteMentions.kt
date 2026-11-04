package app.notekea.notes.presentation.mentions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import app.notekea.notes.domain.NoteEntry
import app.notekea.notes.domain.RelativePath

/** The Linked and Unlinked tabs follow the vault (`ROADMAP.md` 2.5): the active pane's [note] and the vault's [notes]. */
@Composable
fun FollowNoteMentions(
    notes: Map<RelativePath, NoteEntry>,
    onIntent: (NoteMentionsIntent) -> Unit,
    note: RelativePath?
) {
    LaunchedEffect(notes) { onIntent(NoteMentionsIntent.OnNotesChange(notes = notes)) }
    LaunchedEffect(note) { onIntent(NoteMentionsIntent.OnNoteChange(note = note)) }
}
