package app.notekea.core.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons

private const val DISABLED_ALPHA = 0.38f
private const val PREVIEW_NEW_FOLDER = "New folder"
private const val PREVIEW_NEW_NOTE = "New note"
private const val PREVIEW_SHORTCUT = "Ctrl+N"
private const val PREVIEW_SORT = "Sort by name"
private const val TOOLTIP_SEPARATOR = "  "

/**
 * Compact icon-only button for dense desktop toolbars (the sidebar header), 28 dp with the 6 dp control radius.
 * [label] is what screen readers announce and what the tooltip shows on hover, followed by [shortcutHint] when there
 * is one, so an icon is never the only way to learn what a button does. With [takesFocus] false a click leaves the
 * keyboard where it was, as in a text editor's toolbar, so the text keeps its cursor and selection. With
 * [isTooltipBeside] the tooltip shows to its side instead of below, for a vertical strip of buttons (the tool rail).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotekeaIconButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shortcutHint: String? = null,
    takesFocus: Boolean = true,
    isTooltipBeside: Boolean = false,
    tint: Color? = null
) {
    val tooltipText = listOfNotNull(label, shortcutHint).joinToString(separator = TOOLTIP_SEPARATOR)
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
            positioning = if (isTooltipBeside) TooltipAnchorPosition.End else TooltipAnchorPosition.Below
        ),
        tooltip = { PlainTooltip { Text(text = tooltipText) } },
        state = rememberTooltipState(),
        hasAction = false
    ) {
        Box(
            modifier = modifier
                .size(size = NotekeaDimensions.CompactControlSize)
                .focusProperties { canFocus = takesFocus }
                .focusBorder(color = MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.small)
                .clip(shape = MaterialTheme.shapes.small)
                .clickable(enabled = enabled, onClickLabel = label, role = Role.Button, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            val color = tint ?: MaterialTheme.colorScheme.onSurfaceVariant
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(size = NotekeaDimensions.IconSize),
                tint = if (enabled) color else color.copy(alpha = DISABLED_ALPHA)
            )
        }
    }
}

@Preview
@Composable
private fun NotekeaIconButtonPreview() {
    NotekeaPreviewThemes {
        Row(horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs)) {
            NotekeaIconButton(
                icon = NotekeaIcons.NewNote,
                label = PREVIEW_NEW_NOTE,
                onClick = {},
                shortcutHint = PREVIEW_SHORTCUT
            )
            NotekeaIconButton(icon = NotekeaIcons.NewFolder, label = PREVIEW_NEW_FOLDER, onClick = {})
            NotekeaIconButton(icon = NotekeaIcons.SortByName, label = PREVIEW_SORT, onClick = {}, enabled = false)
        }
    }
}
