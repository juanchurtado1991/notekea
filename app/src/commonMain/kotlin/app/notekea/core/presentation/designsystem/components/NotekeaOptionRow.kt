package app.notekea.core.presentation.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions

private const val TINT_ALPHA = 0.10f
private const val HOVER_ALPHA = 0.05f
private const val PREVIEW_WIDTH_DP = 380

/**
 * One of a few ways to go on, as a row of a list: its [title], what it means under it ([supportingText]), what it
 * amounts to at its end ([trailing], "24 pages"), and a [badge] beside the title ("Recommended"). The one the
 * interface suggests [isSuggested] takes the theme's color; the whole row is the button.
 */
@Composable
fun NotekeaOptionRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    trailing: String? = null,
    badge: String? = null,
    isSuggested: Boolean = false
) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.small
    val interaction = remember { MutableInteractionSource() }
    val isHovered by interaction.collectIsHoveredAsState()

    val fill = when {
        isSuggested -> colors.primary.copy(alpha = TINT_ALPHA)
        isHovered -> colors.onSurface.copy(alpha = HOVER_ALPHA)
        else -> Color.Transparent
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(color = fill)
            .border(
                width = NotekeaDimensions.BorderWidth,
                color = if (isSuggested) colors.primary else colors.outlineVariant,
                shape = shape
            )
            .focusBorder(
                color = colors.primary,
                shape = shape
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .padding(
                horizontal = NotekeaDimensions.SpacingM,
                vertical = NotekeaDimensions.SpacingS
            ),
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingM),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(weight = 1f),
            verticalArrangement = Arrangement.spacedBy(space = 2.dp)
        ) {
            OptionTitle(
                title = title,
                badge = badge
            )

            supportingText?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
        }

        trailing?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelLarge,
                color = if (isSuggested) colors.primary else colors.onSurfaceVariant,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
private fun OptionTitle(
    title: String,
    badge: String?
) {
    val colors = MaterialTheme.colorScheme

    // Where the two do not fit on a line the badge goes under the title, whole: squeezed beside it, its word broke
    // in two (the developer's smoke of 2026-11-14).
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS),
        verticalArrangement = Arrangement.spacedBy(space = 2.dp),
        itemVerticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme
                .typography
                .bodyMedium
                .copy(fontWeight = FontWeight.SemiBold),
            color = colors.onSurface
        )

        badge?.let {
            Text(
                text = it,
                modifier = Modifier
                    .background(
                        color = colors.primary,
                        shape = MaterialTheme.shapes.extraSmall
                    )
                    .padding(
                        horizontal = NotekeaDimensions.SpacingXs,
                        vertical = 1.dp
                    ),
                style = MaterialTheme.typography.labelSmall,
                color = colors.onPrimary,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Preview
@Composable
private fun NotekeaOptionRowPreview() {
    NotekeaPreviewThemes {
        Column(
            modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp),
            verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)
        ) {
            NotekeaOptionRow(
                title = "Only this page",
                onClick = {},
                supportingText = "The page you pasted",
                trailing = "1 page"
            )

            NotekeaOptionRow(
                title = "Go deeper",
                onClick = {},
                supportingText = "Also the links inside those pages",
                trailing = "2 levels",
                badge = "Recommended",
                isSuggested = true
            )
        }
    }
}
