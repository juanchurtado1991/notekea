package app.notekea.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons

private const val PREVIEW_LABEL = "Search notes"
private const val PREVIEW_SHORTCUT = "Ctrl+P"
private const val PREVIEW_WIDTH_DP = 260

/**
 * A button drawn like a search field (a magnifier, a muted [label] and, at the end, the [shortcutHint] that opens the
 * same search from anywhere), so search is in sight and its shortcut is learned by seeing it. Clicking it opens the
 * search, usually a [NotekeaPalette]; it never takes typing itself. It fills the width it is given.
 */
@Composable
fun NotekeaSearchButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shortcutHint: String? = null
) {
    val colors = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.small
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height = NotekeaDimensions.CompactControlSize + NotekeaDimensions.SpacingXs)
            .focusBorder(color = colors.primary, shape = shape)
            .clip(shape = shape)
            .background(color = colors.surfaceVariant)
            .border(width = NotekeaDimensions.BorderWidth, color = colors.outlineVariant, shape = shape)
            .clickable(onClickLabel = label, role = Role.Button, onClick = onClick)
            .padding(horizontal = NotekeaDimensions.SpacingS),
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = NotekeaIcons.Find,
            contentDescription = null,
            modifier = Modifier.size(size = NotekeaDimensions.IconSize),
            tint = colors.onSurfaceVariant
        )
        Text(
            text = label,
            modifier = Modifier.weight(weight = 1f),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        shortcutHint?.let {
            Text(text = it, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant, maxLines = 1)
        }
    }
}

@Preview
@Composable
private fun NotekeaSearchButtonPreview() {
    NotekeaPreviewThemes {
        Column(
            modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp),
            verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingS)
        ) {
            NotekeaSearchButton(label = PREVIEW_LABEL, onClick = {}, shortcutHint = PREVIEW_SHORTCUT)
            NotekeaSearchButton(label = PREVIEW_LABEL, onClick = {})
        }
    }
}
