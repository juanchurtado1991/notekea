package app.notekea.core.presentation.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons

private const val DISABLED_ALPHA = 0.38f
private const val PREVIEW_NEW_FOLDER = "New folder"
private const val PREVIEW_NEW_NOTE = "New note"
private const val PREVIEW_SHORTCUT = "Ctrl+N"
private const val PREVIEW_SORT = "Sort by name"

/**
 * Compact icon-only button for dense desktop toolbars. [label] is what screen readers announce and the tooltip shows,
 * followed by [shortcutHint], so an icon is never the only way to learn what a button does. With [takesFocus] false a
 * click leaves the keyboard where it was; with [isTooltipBeside] the tooltip shows to its side, for a vertical strip.
 */
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
    NotekeaTooltip(
        label = label,
        shortcutHint = shortcutHint,
        isBeside = isTooltipBeside
    ) {
        Box(
            modifier = modifier
                .size(size = NotekeaDimensions.CompactControlSize)
                .focusProperties { canFocus = takesFocus }
                .focusBorder(
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.small
                )
                .clip(shape = MaterialTheme.shapes.small)
                .clickable(
                    enabled = enabled,
                    onClickLabel = label,
                    role = Role.Button,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            val color = tint ?: MaterialTheme.colorScheme.onSurfaceVariant

            val iconTint = if (enabled) color else color.copy(alpha = DISABLED_ALPHA)

            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(size = NotekeaDimensions.IconSize),
                tint = iconTint
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

            NotekeaIconButton(
                icon = NotekeaIcons.NewFolder,
                label = PREVIEW_NEW_FOLDER,
                onClick = {}
            )

            NotekeaIconButton(
                icon = NotekeaIcons.SortByName,
                label = PREVIEW_SORT,
                onClick = {},
                enabled = false
            )
        }
    }
}
