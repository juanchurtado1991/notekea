package app.notekea.notes.presentation.tree

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.notes.domain.RelativePath

private const val PREVIEW_BODY = "Planting starts in March.\n\n- [ ] Buy **seeds**\n- [ ] Prepare the beds #garden\n"
private const val PREVIEW_HEIGHT_DP = 320
private const val PREVIEW_WIDTH_DP = 560

/** What the main area shows for a note at [path] in previews: sample text, so the tree needs no editor. */
@Composable
internal fun PreviewNoteContent(
    path: RelativePath?
) {
    Text(
        text = path?.nameWithoutExtension.orEmpty() + "\n\n" + PREVIEW_BODY,
        style = MaterialTheme.typography.bodyLarge
    )
}

@Preview
@Composable
private fun PreviewNoteContentPreview() {
    Box(modifier = Modifier.size(width = PREVIEW_WIDTH_DP.dp, height = PREVIEW_HEIGHT_DP.dp)) {
        PreviewNoteContent(path = TreePreviewData.tree.notes.keys.firstOrNull())
    }
}
