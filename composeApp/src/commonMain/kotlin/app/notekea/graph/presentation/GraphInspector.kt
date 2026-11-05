package app.notekea.graph.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.components.NotekeaButton
import app.notekea.core.presentation.components.NotekeaButtonStyle
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.resources.Res
import app.notekea.resources.graph_connected_to
import app.notekea.resources.graph_link
import app.notekea.resources.graph_link_failed
import app.notekea.resources.graph_link_undo
import app.notekea.resources.graph_linked
import app.notekea.resources.graph_open
import app.notekea.resources.graph_suggested
import org.jetbrains.compose.resources.stringResource

private const val WIDTH_DP = 240
private const val SNIPPET_LINES = 8

/**
 * The selected note, as Finder's inspector shows a file: its name, a few lines, what it connects to, what it could link
 * to, and Open. A link just made can be undone here.
 */
@Composable
internal fun GraphInspector(
    state: GraphState,
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
        if (preview.connected.isNotEmpty()) Connected(preview = preview, onIntent = onIntent)
        if (preview.suggested.isNotEmpty()) Suggested(preview = preview, onIntent = onIntent)
        LinkResult(state = state, onIntent = onIntent)
        NotekeaButton(
            text = stringResource(resource = Res.string.graph_open),
            onClick = { onIntent(GraphIntent.OnOpenSelected) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun Connected(
    preview: GraphPreview,
    onIntent: (GraphIntent) -> Unit
) {
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

@Composable
private fun Suggested(
    preview: GraphPreview,
    onIntent: (GraphIntent) -> Unit
) {
    Text(
        text = stringResource(resource = Res.string.graph_suggested),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    preview.suggested.take(n = MAX_SUGGESTED).forEach { path ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = path.nameWithoutExtension,
                modifier = Modifier
                    .weight(weight = 1f)
                    .clickable { onIntent(GraphIntent.OnNoteSelect(path = path)) },
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            NotekeaButton(
                text = stringResource(resource = Res.string.graph_link),
                onClick = { onIntent(GraphIntent.OnLink(path = path)) },
                style = NotekeaButtonStyle.TEXT
            )
        }
    }
}

/** The link just made, with Undo, or why the note could not be changed. */
@Composable
private fun LinkResult(
    state: GraphState,
    onIntent: (GraphIntent) -> Unit
) {
    val linked = state.linked
    if (state.linkFailed) {
        Text(
            text = stringResource(resource = Res.string.graph_link_failed),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
    } else if (linked != null) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(Res.string.graph_linked, linked.nameWithoutExtension),
                modifier = Modifier.weight(weight = 1f),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            NotekeaButton(
                text = stringResource(resource = Res.string.graph_link_undo),
                onClick = { onIntent(GraphIntent.OnLinkUndo) },
                style = NotekeaButtonStyle.TEXT
            )
        }
    }
}

private const val MAX_CONNECTED = 6
private const val MAX_SUGGESTED = 5

@Preview
@Composable
private fun GraphInspectorPreview() {
    NotekeaPreviewThemes {
        GraphPreviewData.state.let { state ->
            state.preview?.let { GraphInspector(state = state, preview = it, onIntent = {}) }
        }
    }
}
