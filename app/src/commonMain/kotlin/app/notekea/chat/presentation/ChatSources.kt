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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.chat.domain.AnswerSource
import app.notekea.core.presentation.designsystem.components.NotekeaCitationChip
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions

private const val HEADING_SEPARATOR = " › "

/**
 * The notes an answer cites, one per line as a document's references (macOS redesign 7B): the citation's number, then
 * the note and the heading the passage is under; a click opens the passage.
 */
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
        sources.forEach { source ->
            SourceLine(
                source = source,
                onClick = { onSourceClick(source) }
            )
        }
    }
}

@Composable
private fun SourceLine(
    source: AnswerSource,
    onClick: () -> Unit
) {
    val shape = MaterialTheme.shapes.small

    val place = (listOf(source.note.nameWithoutExtension) + source.headingPath.drop(n = 1))
        .joinToString(separator = HEADING_SEPARATOR)

    Row(
        modifier = Modifier
            .clip(shape = shape)
            .pointerHoverIcon(icon = PointerIcon.Hand)
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .padding(
                start = NotekeaDimensions.SpacingXs / 2,
                end = NotekeaDimensions.SpacingS,
                top = NotekeaDimensions.SpacingXs / 2,
                bottom = NotekeaDimensions.SpacingXs / 2
            ),
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs),
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
            color = MaterialTheme.colorScheme.primary,
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
