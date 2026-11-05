package app.notekea.graph.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.components.NotekeaButton
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.resources.Res
import app.notekea.resources.graph_connected_to
import app.notekea.resources.graph_open
import org.jetbrains.compose.resources.stringResource

private const val WIDTH_DP = 240
private const val SNIPPET_LINES = 8

/** The selected note, as Finder's inspector shows a file: its name, a few lines, what it connects to, and Open. */
@Composable
internal fun GraphInspector(
    preview: GraphPreview,
    onIntent: (GraphIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(width = WIDTH_DP.dp)
            .fillMaxHeight()
            .background(color = MaterialTheme.colorScheme.surfaceVariant)
            .padding(all = NotekeaDimensions.SpacingL),
        verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)
    ) {
        Text(text = preview.name, style = MaterialTheme.typography.titleMedium, maxLines = 2)
        if (preview.folder.isNotEmpty()) {
            Text(
                text = preview.folder,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = preview.snippet,
            style = MaterialTheme.typography.bodySmall,
            maxLines = SNIPPET_LINES,
            overflow = TextOverflow.Ellipsis
        )
        if (preview.connected.isNotEmpty()) {
            Text(
                text = stringResource(resource = Res.string.graph_connected_to),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            preview.connected.take(n = MAX_CONNECTED).forEach { path ->
                Text(
                    text = path.nameWithoutExtension,
                    modifier = Modifier.clickable { onIntent(GraphIntent.OnNoteSelect(path = path)) },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )
            }
        }
        NotekeaButton(
            text = stringResource(resource = Res.string.graph_open),
            onClick = { onIntent(GraphIntent.OnOpenSelected) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private const val MAX_CONNECTED = 6

@Preview
@Composable
private fun GraphInspectorPreview() {
    NotekeaPreviewThemes {
        GraphPreviewData.state.preview?.let { GraphInspector(preview = it, onIntent = {}) }
    }
}
