package app.notekea.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions

private const val PREVIEW_EDIT = "Edit"
private const val PREVIEW_PREVIEW = "Preview"
private const val PREVIEW_SHORTCUT = "Ctrl+E"
private const val PREVIEW_WIDTH_DP = 240
private const val TOOLTIP_SEPARATOR = "  "

/**
 * Tabs as wide as their labels, for a toolbar (the editor's Edit | Preview, like GitHub's): the [selectedIndex] one in
 * the primary color with a line under it. The tooltip of each tab shows [shortcutHint] when there is one, so the
 * shortcut that switches them is learned from the tabs themselves. With [takesFocus] false a click leaves the keyboard
 * where it was, so a shortcut and a click never leave the focus ring on a tab that is no longer selected.
 */
@Composable
fun NotekeaCompactTabs(
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    shortcutHint: String? = null,
    takesFocus: Boolean = true
) {
    Row(modifier = modifier.selectableGroup()) {
        labels.forEachIndexed { index, label ->
            CompactTab(
                label = label,
                isSelected = index == selectedIndex,
                onClick = { onSelect(index) },
                takesFocus = takesFocus,
                shortcutHint = shortcutHint
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompactTab(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    takesFocus: Boolean,
    shortcutHint: String?
) {
    val colors = MaterialTheme.colorScheme
    val color = if (isSelected) colors.primary else colors.onSurfaceVariant
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(positioning = TooltipAnchorPosition.Below),
        tooltip = {
            PlainTooltip { Text(text = listOfNotNull(label, shortcutHint).joinToString(separator = TOOLTIP_SEPARATOR)) }
        },
        state = rememberTooltipState(),
        hasAction = false
    ) {
        Column(
            modifier = Modifier
                .focusProperties { canFocus = takesFocus }
                .focusBorder(color = colors.primary, shape = MaterialTheme.shapes.small)
                .clip(shape = MaterialTheme.shapes.small)
                .semantics { selected = isSelected }
                .clickable(role = Role.Tab, onClick = onClick)
                .width(intrinsicSize = IntrinsicSize.Max)
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(
                    horizontal = NotekeaDimensions.SpacingM,
                    vertical = NotekeaDimensions.SpacingXs
                ),
                style = MaterialTheme.typography.labelLarge,
                color = color
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height = NotekeaDimensions.FocusRingWidth)
                    .background(color = if (isSelected) colors.primary else Color.Transparent)
            )
        }
    }
}

@Preview
@Composable
private fun NotekeaCompactTabsPreview() {
    NotekeaPreviewThemes {
        var selected by remember { mutableIntStateOf(value = 0) }
        Box(modifier = Modifier.width(width = PREVIEW_WIDTH_DP.dp)) {
            NotekeaCompactTabs(
                labels = listOf(PREVIEW_EDIT, PREVIEW_PREVIEW),
                selectedIndex = selected,
                onSelect = { selected = it },
                shortcutHint = PREVIEW_SHORTCUT
            )
        }
    }
}
