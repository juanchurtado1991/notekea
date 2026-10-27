package app.notekea.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons

private const val INSET_DP = 2
private const val PREVIEW_CHAT = "Chat"
private const val PREVIEW_INSPECTOR = "Inspector"
private const val SEGMENT_ICON_DP = 14
private const val SEGMENT_VERTICAL_PADDING_DP = 4

/**
 * A few exclusive choices side by side in a soft track, as macOS draws them: each with its icon and label, the
 * [selectedIndex] one raised on the surface with the primary icon. A click goes to [onSelect] with its index; the
 * segments are not focus stops, so a click leaves the keyboard where it was.
 */
@Composable
fun NotekeaSegmentedControl(
    segments: List<NotekeaSegment>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .selectableGroup()
            .clip(shape = MaterialTheme.shapes.small)
            .background(color = colors.surfaceVariant)
            .padding(all = INSET_DP.dp),
        horizontalArrangement = Arrangement.spacedBy(space = INSET_DP.dp)
    ) {
        segments.forEachIndexed { index, segment ->
            Segment(segment = segment, isSelected = index == selectedIndex, onClick = { onSelect(index) })
        }
    }
}

@Composable
private fun Segment(
    segment: NotekeaSegment,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .clip(shape = MaterialTheme.shapes.extraSmall)
            .background(color = if (isSelected) colors.surface else colors.surfaceVariant)
            .focusProperties { canFocus = false }
            .clickable(role = Role.Tab, onClick = onClick)
            .semantics { selected = isSelected }
            .padding(horizontal = NotekeaDimensions.SpacingS, vertical = SEGMENT_VERTICAL_PADDING_DP.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)
    ) {
        Icon(
            imageVector = segment.icon,
            contentDescription = null,
            modifier = Modifier.size(size = SEGMENT_ICON_DP.dp),
            tint = if (isSelected) colors.primary else colors.onSurfaceVariant
        )
        Text(
            text = segment.label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) colors.onSurface else colors.onSurfaceVariant
        )
    }
}

@Preview
@Composable
private fun NotekeaSegmentedControlPreview() {
    NotekeaPreviewThemes {
        NotekeaSegmentedControl(
            segments = listOf(
                NotekeaSegment(icon = NotekeaIcons.Inspector, label = PREVIEW_INSPECTOR),
                NotekeaSegment(icon = NotekeaIcons.Chat, label = PREVIEW_CHAT)
            ),
            selectedIndex = 0,
            onSelect = {}
        )
    }
}
