package app.notekea.core.presentation.designsystem.components

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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons

private const val SEGMENT_GAP_DP = 2
private const val PREVIEW_CHAT = "Chat"
private const val PREVIEW_INSPECTOR = "Inspector"
private const val SEGMENT_ICON_DP = 14
private const val SEGMENT_VERTICAL_PADDING_DP = 4

/**
 * A few exclusive choices side by side in a soft track, as macOS draws them: each with its icon and label, the
 * [selectedIndex] one raised on the surface. A click, or Enter on a segment, goes to [onSelect]
 * with its index.
 */
@Composable
fun NotekeaSegmentedControl(
    segments: List<NotekeaSegment>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .selectableGroup()
            .segmentTrack(colors = MaterialTheme.colorScheme),
        horizontalArrangement = Arrangement.spacedBy(space = SEGMENT_GAP_DP.dp)
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
            .segmentThumb(isSelected = isSelected, colors = colors)
            .focusBorder(color = colors.primary, shape = segmentShape)
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
            tint = if (isSelected) colors.onSurface else colors.onSurfaceVariant
        )
        Text(
            text = segment.label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
            ),
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
