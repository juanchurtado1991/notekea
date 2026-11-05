package app.notekea.query.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.components.NotekeaContextMenuArea
import app.notekea.core.presentation.components.NotekeaContextMenuState
import app.notekea.core.presentation.components.NotekeaMenuItem
import app.notekea.core.presentation.components.focusBorder
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons

private const val PREVIEW_TEXT = "more than"

/** A pop-up button, as in a Mac form: it shows [text], the current choice, and opens [sections] below itself. */
@Composable
internal fun QueryPopup(
    text: String,
    sections: List<List<NotekeaMenuItem>>,
    modifier: Modifier = Modifier
) {
    val menu = remember { NotekeaContextMenuState() }
    val below = belowControl()
    val shape = MaterialTheme.shapes.small
    val colors = MaterialTheme.colorScheme
    NotekeaContextMenuArea(sections = sections, modifier = modifier, state = menu) {
        Row(
            modifier = Modifier
                .heightIn(min = NotekeaDimensions.CompactControlSize)
                .focusBorder(color = colors.primary, shape = shape)
                .clip(shape = shape)
                .background(color = colors.surfaceVariant)
                .pointerHoverIcon(icon = PointerIcon.Hand)
                .clickable(role = Role.Button) { menu.open(at = below) }
                .padding(horizontal = NotekeaDimensions.SpacingS),
            horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                modifier = Modifier.weight(weight = 1f),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                imageVector = NotekeaIcons.Collapse,
                contentDescription = null,
                modifier = Modifier.size(size = NotekeaDimensions.BadgeIconSize),
                tint = colors.onSurfaceVariant
            )
        }
    }
}

/** A toolbar button with its [icon] and [text], as the Mac labels them; [tint] marks the one that writes. */
@Composable
internal fun QueryToolButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    val shape = MaterialTheme.shapes.small
    Row(
        modifier = Modifier
            .heightIn(min = NotekeaDimensions.CompactControlSize)
            .focusBorder(color = MaterialTheme.colorScheme.primary, shape = shape)
            .clip(shape = shape)
            .pointerHoverIcon(icon = PointerIcon.Hand)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = NotekeaDimensions.SpacingXs),
        horizontalArrangement = Arrangement.spacedBy(space = NotekeaDimensions.SpacingXs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(size = NotekeaDimensions.IconSize),
            tint = tint
        )
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = tint, maxLines = 1)
    }
}

/** A [QueryToolButton] that opens [sections] below itself. */
@Composable
internal fun QueryToolMenu(
    icon: ImageVector,
    text: String,
    sections: List<List<NotekeaMenuItem>>
) {
    val menu = remember { NotekeaContextMenuState() }
    val below = belowControl()
    NotekeaContextMenuArea(sections = sections, state = menu) {
        QueryToolButton(icon = icon, text = text, onClick = { menu.open(at = below) })
    }
}

@Composable
private fun belowControl(): Offset =
    with(LocalDensity.current) { Offset(x = 0f, y = NotekeaDimensions.CompactControlSize.toPx()) }

@Preview
@Composable
private fun QueryPopupPreview() {
    NotekeaPreviewThemes { QueryPopup(text = PREVIEW_TEXT, sections = emptyList()) }
}

@Preview
@Composable
private fun QueryToolButtonPreview() {
    NotekeaPreviewThemes { QueryToolButton(icon = NotekeaIcons.QuerySort, text = PREVIEW_TEXT, onClick = {}) }
}

@Preview
@Composable
private fun QueryToolMenuPreview() {
    NotekeaPreviewThemes { QueryToolMenu(icon = NotekeaIcons.QuerySort, text = PREVIEW_TEXT, sections = emptyList()) }
}
