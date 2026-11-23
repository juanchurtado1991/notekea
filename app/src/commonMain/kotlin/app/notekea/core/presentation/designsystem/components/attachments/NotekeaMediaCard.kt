package app.notekea.core.presentation.designsystem.components.attachments

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.designsystem.components.focusBorder
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions

private const val PREVIEW_LEAD_DP = 56
private const val PREVIEW_TITLE = "menu.pdf"
private const val PREVIEW_SUBTITLE = "Opens in a tab"

/**
 * A file in a note as a card: a picture of it ([lead]) beside its [title] and, smaller, a [subtitle]; a click on any
 * of it acts ([onClick], which [clickLabel] names for screen readers). What a document and a video share.
 */
@Composable
fun NotekeaMediaCard(
    title: String,
    subtitle: String,
    clickLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    lead: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    Surface(
        modifier = modifier
            .focusBorder(
                color = colors.primary,
                shape = MaterialTheme.shapes.small
            )
            .clip(shape = MaterialTheme.shapes.small)
            .clickable(
                onClickLabel = clickLabel,
                role = Role.Button,
                onClick = onClick
            ),
        shape = MaterialTheme.shapes.small,
        color = colors.surface,
        border = BorderStroke(
            width = NotekeaDimensions.BorderWidth,
            color = colors.outline
        )
    ) {
        Row(
            modifier = Modifier.padding(all = NotekeaDimensions.SpacingS),
            horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            lead()

            Column(
                verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Preview
@Composable
private fun NotekeaMediaCardPreview() {
    NotekeaPreviewThemes {
        NotekeaMediaCard(
            title = PREVIEW_TITLE,
            subtitle = PREVIEW_SUBTITLE,
            clickLabel = PREVIEW_SUBTITLE,
            onClick = {}
        ) {
            Box(
                modifier = Modifier
                    .size(size = PREVIEW_LEAD_DP.dp)
                    .background(color = MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    }
}
