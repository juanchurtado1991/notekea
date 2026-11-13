package app.notekea.graph.presentation.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.presentation.designsystem.components.NotekeaButton
import app.notekea.core.presentation.designsystem.components.NotekeaButtonStyle
import app.notekea.core.presentation.designsystem.components.NotekeaSectionHeader
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.graph.presentation.model.GraphIntent
import app.notekea.graph.presentation.model.GraphPreview
import app.notekea.graph.presentation.model.GraphPreviewData
import app.notekea.graph.presentation.model.GraphState
import app.notekea.resources.Res
import app.notekea.resources.graph_connected_to
import app.notekea.resources.graph_inspector_empty
import app.notekea.resources.graph_link
import app.notekea.resources.graph_link_failed
import app.notekea.resources.graph_link_undo
import app.notekea.resources.graph_linked
import app.notekea.resources.graph_open
import app.notekea.resources.graph_suggested
import org.jetbrains.compose.resources.stringResource

private const val SNIPPET_LINES = 8
private const val MAX_CONNECTED = 6
private const val MAX_SUGGESTED = 5

/**
 * The side panel's inspector while the graph is shown: the selected note, as Finder's inspector shows a file, with its
 * name and a few lines, then what it connects to and what it could link to, under the panel's own section headers,
 * with Open always in reach at the panel's foot. A link just made can be undone here.
 */
@Composable
fun GraphInspector(
    state: GraphState,
    onIntent: (GraphIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val preview = state.preview

    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(weight = 1f)
                .fillMaxWidth()
                .verticalScroll(state = rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)
        ) {
            if (preview == null) {
                Muted(
                    text = stringResource(resource = Res.string.graph_inspector_empty),
                    modifier = InsetModifier
                )
            } else {
                Summary(preview = preview)

                Connected(
                    preview = preview,
                    onIntent = onIntent
                )

                Suggested(
                    preview = preview,
                    onIntent = onIntent
                )

                LinkResult(
                    state = state,
                    onIntent = onIntent
                )
            }
        }

        if (preview != null) {
            NotekeaButton(
                text = stringResource(resource = Res.string.graph_open),
                onClick = { onIntent(GraphIntent.OnOpenSelected) },
                modifier = Modifier.fillMaxWidth().padding(all = NotekeaDimensions.SpacingS),
                style = NotekeaButtonStyle.SECONDARY
            )
        }
    }
}

/** Where a section header's title starts and its count ends, so the text lines up under them. */
private val InsetModifier = Modifier.padding(
    start = NotekeaDimensions.SpacingM * 2,
    end = NotekeaDimensions.SpacingS
)

@Composable
private fun Summary(preview: GraphPreview) {
    Column(
        modifier = InsetModifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)
    ) {
        Text(
            text = preview.name,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2
        )

        if (preview.folder.isNotEmpty()) Muted(text = preview.folder)

        Text(
            text = preview.snippet,
            style = MaterialTheme.typography.bodySmall,
            maxLines = SNIPPET_LINES,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun Muted(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun Connected(
    preview: GraphPreview,
    onIntent: (GraphIntent) -> Unit
) {
    if (preview.connected.isEmpty()) return

    NotekeaSectionHeader(
        title = stringResource(resource = Res.string.graph_connected_to),
        count = preview.connected.size
    )

    preview
        .connected
        .take(n = MAX_CONNECTED)
        .forEach { path ->
            NoteRow(
                path = path,
                color = MaterialTheme.colorScheme.primary,
                onIntent = onIntent
            )
        }
}

@Composable
private fun Suggested(
    preview: GraphPreview,
    onIntent: (GraphIntent) -> Unit
) {
    if (preview.suggested.isEmpty()) return

    NotekeaSectionHeader(
        title = stringResource(resource = Res.string.graph_suggested),
        count = preview.suggested.size
    )

    preview
        .suggested
        .take(n = MAX_SUGGESTED)
        .forEach { path ->
            NoteRow(
                path = path,
                color = MaterialTheme.colorScheme.onSurface,
                onIntent = onIntent
            ) {
                TextAction(
                    text = stringResource(resource = Res.string.graph_link),
                    onClick = { onIntent(GraphIntent.OnLink(path = path)) }
                )
            }
        }
}

/** A note's name, which selects it, and what can be done with it at the end. */
@Composable
private fun NoteRow(
    path: RelativePath,
    color: Color,
    onIntent: (GraphIntent) -> Unit,
    action: @Composable () -> Unit = {}
) {
    Row(
        modifier = InsetModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = path.nameWithoutExtension,
            modifier = Modifier
                .weight(weight = 1f)
                .pointerHoverIcon(icon = PointerIcon.Hand)
                .clickable(role = Role.Button) { onIntent(GraphIntent.OnNoteSelect(path = path)) },
            style = MaterialTheme.typography.bodySmall,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        action()
    }
}

/** A small action at a row's end, the size of the row's own text. */
@Composable
private fun TextAction(
    text: String,
    onClick: () -> Unit
) {
    Text(
        text = text,
        modifier = Modifier
            .pointerHoverIcon(icon = PointerIcon.Hand)
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .padding(start = NotekeaDimensions.SpacingS),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.primary
    )
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
            modifier = InsetModifier,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
    } else if (linked != null) {
        Row(
            modifier = InsetModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Muted(
                text = stringResource(Res.string.graph_linked, linked.nameWithoutExtension),
                modifier = Modifier.weight(weight = 1f)
            )

            TextAction(
                text = stringResource(resource = Res.string.graph_link_undo),
                onClick = { onIntent(GraphIntent.OnLinkUndo) }
            )
        }
    }
}

@Preview
@Composable
private fun GraphInspectorPreview() {
    NotekeaPreviewThemes {
        GraphInspector(
            state = GraphPreviewData.state,
            onIntent = {}
        )
    }
}

@Preview
@Composable
private fun GraphInspectorEmptyPreview() {
    NotekeaPreviewThemes {
        GraphInspector(
            state = GraphPreviewData.state.copy(preview = null),
            onIntent = {}
        )
    }
}
