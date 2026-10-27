package app.notekea.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons

private const val PREVIEW_HEIGHT_DP = 260
private const val RAIL_WIDTH_DP = 40

/**
 * A narrow strip of icon buttons along the window's edge that stays when the panel beside it is hidden, as in IDEs
 * (Android Studio, VS Code): the actions always at hand, each with a tooltip beside it, naming it and its shortcut. A
 * [NotekeaToolRailItem.isSelected] button (the panel it shows is open) sits on the selected color. [bottomItems] sit at
 * the strip's bottom, apart from the daily ones, as Obsidian keeps its vault and settings there. [label] names the
 * strip for screen readers.
 */
@Composable
fun NotekeaToolRail(
    items: List<NotekeaToolRailItem>,
    label: String,
    modifier: Modifier = Modifier,
    bottomItems: List<NotekeaToolRailItem> = emptyList()
) {
    val colors = MaterialTheme.colorScheme
    Row(modifier = modifier.fillMaxHeight()) {
        Column(
            modifier = Modifier
                .width(width = RAIL_WIDTH_DP.dp)
                .fillMaxHeight()
                .background(color = colors.background)
                .padding(vertical = NotekeaDimensions.SpacingS)
                .semantics { contentDescription = label }
                .selectableGroup(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)
        ) {
            items.forEach { RailButton(item = it) }
            Spacer(modifier = Modifier.weight(weight = 1f))
            bottomItems.forEach { RailButton(item = it) }
        }
        Box(
            modifier = Modifier
                .width(width = NotekeaDimensions.BorderWidth)
                .fillMaxHeight()
                .background(color = colors.outlineVariant)
        )
    }
}

/** One button of the rail, on the selected color while [NotekeaToolRailItem.isSelected], its tooltip beside it. */
@Composable
private fun RailButton(
    item: NotekeaToolRailItem
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .background(
                color = if (item.isSelected) colors.primaryContainer else colors.background,
                shape = MaterialTheme.shapes.small
            )
            .semantics { selected = item.isSelected }
    ) {
        NotekeaIconButton(
            icon = item.icon,
            label = item.label,
            onClick = item.onClick,
            shortcutHint = item.shortcutHint,
            takesFocus = false,
            isTooltipBeside = true
        )
    }
}

@Preview
@Composable
private fun NotekeaToolRailPreview() {
    NotekeaPreviewThemes {
        NotekeaToolRail(
            items = listOf(
                NotekeaToolRailItem(icon = NotekeaIcons.Files, label = "Files", onClick = {}, isSelected = true),
                NotekeaToolRailItem(icon = NotekeaIcons.Find, label = "Search notes", onClick = {}),
                NotekeaToolRailItem(icon = NotekeaIcons.NewNote, label = "New note", onClick = {})
            ),
            label = "Sidebar tools",
            modifier = Modifier.height(height = PREVIEW_HEIGHT_DP.dp)
        )
    }
}
