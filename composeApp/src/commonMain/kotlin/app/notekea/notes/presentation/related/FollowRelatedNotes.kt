package app.notekea.notes.presentation.related

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import app.notekea.core.notes.domain.RelativePath

/**
 * The Related section follows the vault (`ROADMAP.md` 2.4): the active pane's [note] and its editor's [buffer] at
 * every edit, the vault's [notes], and whether the index is ready ([isIndexReady], which changes as runs end).
 */
@Composable
fun FollowRelatedNotes(
    buffer: TextFieldState,
    notes: Collection<RelativePath>,
    isIndexReady: Boolean,
    onIntent: (RelatedNotesIntent) -> Unit,
    note: RelativePath?
) {
    LaunchedEffect(notes) { onIntent(RelatedNotesIntent.OnNotesChange(notes = notes.toList())) }
    LaunchedEffect(isIndexReady) { onIntent(RelatedNotesIntent.OnIndexChange(isReady = isIndexReady)) }
    LaunchedEffect(note, buffer) {
        snapshotFlow { buffer.text.toString() }.collect { text ->
            onIntent(RelatedNotesIntent.OnNoteChange(text = text, note = note))
        }
    }
}
