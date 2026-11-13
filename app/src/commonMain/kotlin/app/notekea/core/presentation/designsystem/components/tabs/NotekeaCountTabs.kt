package app.notekea.core.presentation.designsystem.components.tabs

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
import androidx.compose.foundation.shape.CircleShape
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
import app.notekea.core.presentation.designsystem.components.fields.segmentShape
import app.notekea.core.presentation.designsystem.components.fields.segmentThumb
import app.notekea.core.presentation.designsystem.components.fields.segmentTrack
import app.notekea.core.presentation.designsystem.components.focusBorder
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions

private const val HEIGHT_DP = 26
private const val COUNT_GAP_DP = 5
private const val EMPTY_ALPHA = 0.5f
private const val PILL_ALPHA = 0.18f
private const val PILL_PADDING_DP = 6
private const val PREVIEW_WIDTH_DP = 300

/**
 * A compact segmented picker whose segments say how many each holds, as macOS inspectors switch views: [labels] with
 * their [counts] (none shown when null) on a muted track, the [selectedIndex] one raised on the surface with its count
 * in the theme's color. Each segment is a tab a click, Tab and Enter (or Space) choose ([onSelect]).
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
            .segmentTrack(colors = MaterialTheme.colorScheme)
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

    Row(
        modifier = Modifier
            .weight(weight = 1f)
            .fillMaxHeight()
            .segmentThumb(
                isSelected = isSelected,
                colors = colors
            )
            .focusBorder(
                color = colors.primary,
                shape = segmentShape
            )
            .selectable(
                selected = isSelected,
                role = Role.Tab,
                onClick = onClick
            ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme
                .typography
                .labelMedium
                .copy(fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium),
            color = if (isSelected) colors.onSurface else colors.onSurfaceVariant,
            maxLines = 1
        )

        if (count != null) {
            Spacer(modifier = Modifier.width(width = COUNT_GAP_DP.dp))

            TabCount(
                count = count,
                isSelected = isSelected
            )
        }
    }
}

/** How many a tab holds: a pill in the theme's color on the selected tab, fainter when it holds none. */
@Composable
private fun TabCount(
    count: Int,
    isSelected: Boolean
) {
    val colors = MaterialTheme.colorScheme
    val isPill = isSelected && count > 0

    val color = when {
        isPill -> colors.primary
        count == 0 -> colors.onSurfaceVariant.copy(alpha = EMPTY_ALPHA)
        else -> colors.onSurfaceVariant
    }

    val pill = if (isPill) {
        Modifier
            .background(
                color = colors.primary.copy(alpha = PILL_ALPHA),
                shape = CircleShape
            )
            .padding(horizontal = PILL_PADDING_DP.dp)
    } else {
        Modifier
    }

    Text(
        text = count.toString(),
        modifier = pill,
        style = MaterialTheme.typography.labelSmall,
        color = color
    )
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
