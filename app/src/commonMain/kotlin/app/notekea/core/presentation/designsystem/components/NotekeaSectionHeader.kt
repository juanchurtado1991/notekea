package app.notekea.core.presentation.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.designsystem.components.feedback.NotekeaTooltip
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons

private const val PREVIEW_BOOKMARKS = "Bookmarks"
private const val PREVIEW_COUNT = 4
private const val PREVIEW_FILES = "Files"
private const val PREVIEW_WIDTH_DP = 280

/**
 * A section's header as macOS's sidebars draw it (2A): its [title] small, bold and muted, with a chevron showing
 * whether it [isOpen] when it folds ([onToggle]), and [count] at the end. Only a header with a [shortcutHint] has a
 * tooltip: one repeating the title would cover the section's first row.
 */
@Composable
fun NotekeaSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    isOpen: Boolean = true,
    onToggle: (() -> Unit)? = null,
    count: Int? = null,
    shortcutHint: String? = null
) {
    Box(modifier = modifier.fillMaxWidth().padding(horizontal = NotekeaDimensions.SpacingXs)) {
        if (shortcutHint == null) {
            HeaderRow(
                title = title,
                isOpen = isOpen,
                onToggle = onToggle,
                count = count
            )
        } else {
            NotekeaTooltip(
                label = title,
                shortcutHint = shortcutHint
            ) {
                HeaderRow(
                    title = title,
                    isOpen = isOpen,
                    onToggle = onToggle,
                    count = count
                )
            }
        }
    }
}

@Composable
private fun HeaderRow(
    title: String,
    isOpen: Boolean,
    onToggle: (() -> Unit)?,
    count: Int?
) {
    val colors = MaterialTheme.colorScheme
    val muted = colors.onSurfaceVariant
    val shape = MaterialTheme.shapes.small

    val click = onToggle?.let {
        Modifier.clickable(
            role = Role.Button,
            onClick = it
        )
    } ?: Modifier

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = NotekeaDimensions.CompactControlSize)
            .focusBorder(
                color = colors.primary,
                shape = shape
            )
            .clip(shape = shape)
            .then(other = click)
            .padding(horizontal = NotekeaDimensions.SpacingXs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)
    ) {
        if (onToggle != null) {
            Icon(
                imageVector = if (isOpen) NotekeaIcons.Collapse else NotekeaIcons.Expand,
                contentDescription = null,
                modifier = Modifier.size(size = NotekeaDimensions.BadgeIconSize),
                tint = muted
            )
        } else {
            Spacer(modifier = Modifier.width(width = NotekeaDimensions.BadgeIconSize))
        }

        Text(
            text = title,
            modifier = Modifier.weight(weight = 1f),
            style = MaterialTheme
                .typography
                .labelMedium
                .copy(fontWeight = FontWeight.SemiBold),
            color = muted
        )

        count?.let {
            Text(
                text = it.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = muted
            )
        }
    }
}

@Preview
@Composable
private fun NotekeaSectionHeaderPreview() {
    NotekeaPreviewThemes {
        Column(modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)) {
            NotekeaSectionHeader(
                title = PREVIEW_BOOKMARKS,
                onToggle = {},
                count = PREVIEW_COUNT
            )

            NotekeaSectionHeader(title = PREVIEW_FILES)
        }
    }
}
