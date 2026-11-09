package app.notekea.core.presentation.designsystem.components

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
import androidx.compose.ui.tooling.preview.Preview
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons

private const val PREVIEW_TEXT = "Sort"

/** A toolbar button with its [icon] and [text], as the Mac labels them; [tint] marks the one that writes. */
@Composable
fun NotekeaToolButton(
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

/** A [NotekeaToolButton] that opens [sections] below itself. */
@Composable
fun NotekeaToolMenu(
    icon: ImageVector,
    text: String,
    sections: List<List<NotekeaMenuItem>>
) {
    val menu = remember { NotekeaContextMenuState() }
    val below = with(LocalDensity.current) { Offset(x = 0f, y = NotekeaDimensions.CompactControlSize.toPx()) }
    NotekeaContextMenuArea(sections = sections, state = menu) {
        NotekeaToolButton(icon = icon, text = text, onClick = { menu.open(at = below) })
    }
}

@Preview
@Composable
private fun NotekeaToolButtonPreview() {
    NotekeaPreviewThemes { NotekeaToolButton(icon = NotekeaIcons.QuerySort, text = PREVIEW_TEXT, onClick = {}) }
}

@Preview
@Composable
private fun NotekeaToolMenuPreview() {
    NotekeaPreviewThemes { NotekeaToolMenu(icon = NotekeaIcons.QuerySort, text = PREVIEW_TEXT, sections = emptyList()) }
}
