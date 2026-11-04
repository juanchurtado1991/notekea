package app.notekea.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions

private const val HEIGHT_DP = 26
private const val INSET_DP = 2
private const val TRACK_RADIUS_DP = 7
private const val THUMB_RADIUS_DP = 5
private const val COUNT_GAP_DP = 5
private const val PREVIEW_WIDTH_DP = 300

/**
 * A compact segmented picker whose segments say how many each holds, as macOS inspectors switch views: [labels] with
 * their [counts] (none shown when null) on a muted track, the [selectedIndex] one raised on the surface. Each segment
 * is a tab a click, Tab and Enter (or Space) choose ([onSelect]).
 */
@Composable
fun NotekeaCountTabs(
    labels: List<String>,
    counts: List<Int?>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height = HEIGHT_DP.dp)
            .clip(shape = RoundedCornerShape(size = TRACK_RADIUS_DP.dp))
            .background(color = MaterialTheme.colorScheme.surfaceVariant)
            .padding(all = INSET_DP.dp)
            .selectableGroup()
    ) {
        labels.forEachIndexed { index, label ->
            CountTab(
                label = label,
                count = counts.getOrNull(index = index),
                isSelected = index == selectedIndex,
                onClick = { onSelect(index) }
            )
        }
    }
}

@Composable
private fun RowScope.CountTab(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    count: Int?
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(size = THUMB_RADIUS_DP.dp)
    Row(
        modifier = Modifier
            .weight(weight = 1f)
            .fillMaxHeight()
            .clip(shape = shape)
            .background(color = if (isSelected) colors.surface else Color.Transparent)
            .border(
                width = NotekeaDimensions.BorderWidth,
                color = if (isSelected) colors.outlineVariant else Color.Transparent,
                shape = shape
            )
            .focusBorder(color = colors.primary, shape = shape)
            .selectable(selected = isSelected, role = Role.Tab, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
            ),
            color = if (isSelected) colors.onSurface else colors.onSurfaceVariant,
            maxLines = 1
        )
        if (count != null) {
            Spacer(modifier = Modifier.width(width = COUNT_GAP_DP.dp))
            Text(text = count.toString(), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
        }
    }
}

@Preview
@Composable
private fun NotekeaCountTabsPreview() {
    NotekeaPreviewThemes {
        NotekeaCountTabs(
            labels = listOf("Related", "Linked", "Unlinked"),
            counts = listOf(5, 2, null),
            selectedIndex = 0,
            onSelect = {},
            modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)
        )
    }
}
