package app.notekea.chat.presentation

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import app.notekea.chat.domain.AnswerSource
import app.notekea.core.presentation.designsystem.components.NotekeaCitationChip
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions

private const val HEADING_SEPARATOR = " › "
private const val PILL_RADIUS_DP = 12
private const val PILL_MAX_WIDTH_DP = 220

/** The notes an answer cites, as small pills of their number and place; a click opens the passage. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ChatSources(
    sources: List<AnswerSource>,
    onSourceClick: (AnswerSource) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs),
        verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)
    ) {
        sources.forEach { source ->
            SourcePill(
                source = source,
                onClick = { onSourceClick(source) }
            )
        }
    }
}

@Composable
private fun SourcePill(
    source: AnswerSource,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(size = PILL_RADIUS_DP.dp)

    val place = (listOf(source.note.nameWithoutExtension) + source.headingPath.drop(n = 1))
        .joinToString(separator = HEADING_SEPARATOR)

    Row(
        modifier = Modifier
            .widthIn(max = PILL_MAX_WIDTH_DP.dp)
            .clip(shape = shape)
            .border(
                width = NotekeaDimensions.BorderWidth,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = shape
            )
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
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
