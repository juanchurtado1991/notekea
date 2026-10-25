package app.notekea.notes.presentation.tree

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.preview.PreviewPlatformConventions
import app.notekea.notes.domain.NoteEntry
import app.notekea.notes.presentation.editor.EditorContent
import app.notekea.notes.presentation.editor.EditorState

private const val PREVIEW_BODY = "Planting starts in March.\n\n- [ ] Buy **seeds**\n- [ ] Prepare the beds #garden\n"
private const val PREVIEW_HEIGHT_DP = 320
private const val PREVIEW_WIDTH_DP = 560

/** What the main area shows for a selected [note] in previews: the editor with sample text, no ViewModel. */
@Composable
internal fun PreviewNoteContent(
    note: NoteEntry
) {
    val buffer = remember(note) { TextFieldState(initialText = PREVIEW_BODY) }
    EditorContent(
        state = EditorState(notePath = note.file.path),
        buffer = buffer,
        platformConventions = PreviewPlatformConventions.linux,
        onIntent = {},
        properties = note.metadata
    )
}

@Preview
@Composable
private fun PreviewNoteContentPreview() {
    Box(modifier = Modifier.size(width = PREVIEW_WIDTH_DP.dp, height = PREVIEW_HEIGHT_DP.dp)) {
        TreePreviewData.tree.notes.values.firstOrNull()?.let { PreviewNoteContent(note = it) }
    }
}
