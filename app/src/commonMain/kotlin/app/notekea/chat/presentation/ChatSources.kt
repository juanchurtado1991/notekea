package app.notekea.chat.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.chat.domain.AnswerSource
import app.notekea.core.presentation.designsystem.components.NotekeaCitationChip
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.resources.Res
import app.notekea.resources.chat_sources
import org.jetbrains.compose.resources.stringResource

private const val HEADING_SEPARATOR = " › "

/** The notes an answer cites, each with its number and the heading of its passage; a click opens the passage. */
@Composable
internal fun ChatSources(
    sources: List<AnswerSource>,
    onSourceClick: (AnswerSource) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)
    ) {
        Text(
            text = stringResource(resource = Res.string.chat_sources).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        sources.forEach { source ->
            SourceRow(
                source = source,
                onClick = { onSourceClick(source) }
            )
        }
    }
}

@Composable
private fun SourceRow(
    source: AnswerSource,
    onClick: () -> Unit
) {
    val headings = source.headingPath.drop(n = 1)

    val place = (listOf(source.note.nameWithoutExtension) + headings)
        .joinToString(separator = HEADING_SEPARATOR)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .pointerHoverIcon(icon = PointerIcon.Hand)
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .padding(vertical = NotekeaDimensions.SpacingXs / 2),
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NotekeaCitationChip(
            number = source.number,
            snippet = source.text,
            onClick = onClick
        )

        Text(
            text = place,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview
@Composable
private fun ChatSourcesPreview() {
    NotekeaPreviewThemes {
        ChatSources(
            sources = ChatPreviewData.sources,
            onSourceClick = {}
        )
    }
}
