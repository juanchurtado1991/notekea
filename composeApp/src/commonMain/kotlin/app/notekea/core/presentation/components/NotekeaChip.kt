package app.notekea.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons

private const val PREVIEW_REMOVE = "Remove"
private val PREVIEW_ITEMS = listOf("recipe", "venezuelan", "breakfast")

/**
 * One item of a list shown as a pill (a tag, an alias): its [label] on the theme's soft surface, and a small ✕ when
 * [onRemove] is given, named [removeLabel] for screen readers. The ✕ is not a focus stop: the list's field is.
 */
@Composable
fun NotekeaChip(
    label: String,
    modifier: Modifier = Modifier,
    onRemove: (() -> Unit)? = null,
    removeLabel: String? = null
) {
    val shape = RoundedCornerShape(percent = FULL_ROUND)
    val endPadding = if (onRemove == null) NotekeaDimensions.SpacingS else 0.dp
    Row(
        modifier = modifier
            .clip(shape = shape)
            .background(color = MaterialTheme.colorScheme.surfaceVariant, shape = shape)
            .padding(start = NotekeaDimensions.SpacingS, end = endPadding),
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs / 2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        onRemove?.let { ChipRemove(onRemove = it, removeLabel = removeLabel) }
    }
}

private const val FULL_ROUND = 50

/** The chip's small ✕. */
@Composable
private fun ChipRemove(
    onRemove: () -> Unit,
    removeLabel: String?
) {
    val shape = RoundedCornerShape(percent = FULL_ROUND)
    Box(
        modifier = Modifier
            .focusProperties { canFocus = false }
            .clip(shape = shape)
            .clickable(onClickLabel = removeLabel, role = Role.Button, onClick = onRemove)
            .padding(all = NotekeaDimensions.SpacingXs),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = NotekeaIcons.Close,
            contentDescription = removeLabel,
            modifier = Modifier.size(size = NotekeaDimensions.BadgeIconSize),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview
@Composable
private fun NotekeaChipPreview() {
    NotekeaPreviewThemes {
        Row(horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)) {
            PREVIEW_ITEMS.forEach { NotekeaChip(label = it, onRemove = {}, removeLabel = PREVIEW_REMOVE) }
            NotekeaChip(label = PREVIEW_REMOVE)
        }
    }
}
