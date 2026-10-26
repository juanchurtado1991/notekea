package app.notekea.core.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import app.notekea.core.presentation.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.theme.NotekeaDimensions
import app.notekea.core.presentation.theme.NotekeaIcons

private const val PREVIEW_DELETE = "Delete note"
private const val PREVIEW_DELETE_SHORTCUT = "Delete"
private const val PREVIEW_HEIGHT_DP = 300
private const val PREVIEW_NEW_NOTE = "New note"
private const val PREVIEW_NEW_SHORTCUT = "Ctrl+N"
private const val PREVIEW_RENAME = "Rename"
private const val PREVIEW_RENAME_SHORTCUT = "F2"
private const val PREVIEW_REVEAL = "Show in Files"
private const val PREVIEW_ROW = "Right-click here"
private const val PREVIEW_WIDTH_DP = 320
private const val SHORTCUT_MIN_WIDTH_DP = 48

/**
 * Wraps [content] with a context menu of [sections] (separated by dividers) that opens at the pointer on a right
 * click, or wherever [state] is opened from the keyboard. Areas can nest: a right click opens only the innermost
 * menu. [onOpenByPointer] runs on that right click, so the clicked item can be selected first, as file managers do.
 * Choosing an action closes the menu first, unless it [NotekeaMenuItem.keepsMenuOpen]. With [takesFocus] false the
 * menu does not take the keyboard, so a text field keeps its selection while it is open. The menu is themed like the rest of the app (surface, outline border,
 * 6 dp corners) on every platform, with the 32 dp rows of desktop menus and shortcuts aligned at the end.
 */
@Composable
fun NotekeaContextMenuArea(
    sections: List<List<NotekeaMenuItem>>,
    modifier: Modifier = Modifier,
    state: NotekeaContextMenuState = remember { NotekeaContextMenuState() },
    onOpenByPointer: () -> Unit = {},
    takesFocus: Boolean = true,
    content: @Composable () -> Unit
) {
    var areaHeight by remember { mutableIntStateOf(value = 0) }
    Box(
        modifier = modifier
            .onSizeChanged { areaHeight = it.height }
            .onSecondaryPress {
                onOpenByPointer()
                state.open(at = it)
            }
    ) {
        content()
        val position = state.position
        DropdownMenu(
            expanded = position != null,
            onDismissRequest = state::close,
            offset = menuOffset(position = position ?: Offset.Zero, areaHeight = areaHeight),
            properties = PopupProperties(focusable = takesFocus),
            shape = MaterialTheme.shapes.small,
            containerColor = MaterialTheme.colorScheme.surface,
            border = BorderStroke(width = NotekeaDimensions.BorderWidth, color = MaterialTheme.colorScheme.outline)
        ) {
            sections.forEachIndexed { index, items ->
                if (index > 0) MenuDivider()
                items.forEach { MenuRow(item = it, onChosen = state::close) }
            }
        }
    }
}

@Composable
private fun MenuRow(
    item: NotekeaMenuItem,
    onChosen: () -> Unit
) {
    val color = if (item.isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    DropdownMenuItem(
        text = { Text(text = item.label, style = MaterialTheme.typography.bodyMedium, color = color) },
        onClick = {
            if (!item.keepsMenuOpen) onChosen()
            item.onClick()
        },
        modifier = Modifier.height(height = NotekeaDimensions.MenuItemHeight),
        leadingIcon = leadingIconOf(item = item, color = color),
        trailingIcon = item.shortcutHint?.let { hint ->
            {
                Text(
                    text = hint,
                    modifier = Modifier.widthIn(min = SHORTCUT_MIN_WIDTH_DP.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End
                )
            }
        },
        enabled = item.enabled
    )
}

/** The item's icon, or for a choice its check mark (an empty slot of the same size when it is not chosen). */
private fun leadingIconOf(
    item: NotekeaMenuItem,
    color: Color
): (@Composable () -> Unit)? {
    val icon = if (item.isChecked != null) NotekeaIcons.Checked.takeIf { item.isChecked == true } else item.icon
    if (icon == null && item.isChecked == null) return null
    return {
        val iconModifier = Modifier.size(size = NotekeaDimensions.IconSize)
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = iconModifier, tint = color)
        } else {
            Box(modifier = iconModifier)
        }
    }
}

@Composable
private fun MenuDivider() {
    Box(
        modifier = Modifier
            .padding(vertical = NotekeaDimensions.SpacingXs)
            .fillMaxWidth()
            .height(height = NotekeaDimensions.BorderWidth)
            .background(color = MaterialTheme.colorScheme.outline)
    )
}

/** The menu is laid out below its anchor, so the pointer position is turned into an offset from the anchor's bottom. */
@Composable
private fun menuOffset(
    position: Offset,
    areaHeight: Int
): DpOffset = with(LocalDensity.current) { DpOffset(x = position.x.toDp(), y = (position.y - areaHeight).toDp()) }

private fun Modifier.onSecondaryPress(
    onPress: (Offset) -> Unit
): Modifier = pointerInput(onPress) {
    awaitEachGesture {
        val event = awaitPointerEvent()
        // A nested area (a row inside a list that has its own menu) handles the press first and consumes it.
        val isUnhandled = event.changes.none { it.isConsumed }
        if (event.type == PointerEventType.Press && event.buttons.isSecondaryPressed && isUnhandled) {
            event.changes.forEach { it.consume() }
            onPress(event.changes.first().position)
        }
    }
}

@Preview
@Composable
private fun NotekeaContextMenuAreaPreview() {
    val state = remember { NotekeaContextMenuState().apply { open() } }
    NotekeaPreviewThemes {
        Box(modifier = Modifier.size(width = PREVIEW_WIDTH_DP.dp, height = PREVIEW_HEIGHT_DP.dp)) {
            NotekeaContextMenuArea(
                sections = listOf(
                    listOf(
                        NotekeaMenuItem(
                            label = PREVIEW_NEW_NOTE,
                            onClick = {},
                            icon = NotekeaIcons.NewNote,
                            shortcutHint = PREVIEW_NEW_SHORTCUT
                        )
                    ),
                    listOf(
                        NotekeaMenuItem(
                            label = PREVIEW_RENAME,
                            onClick = {},
                            icon = NotekeaIcons.Rename,
                            shortcutHint = PREVIEW_RENAME_SHORTCUT
                        ),
                        NotekeaMenuItem(label = PREVIEW_REVEAL, onClick = {}, icon = NotekeaIcons.Reveal)
                    ),
                    listOf(
                        NotekeaMenuItem(
                            label = PREVIEW_DELETE,
                            onClick = {},
                            icon = NotekeaIcons.Delete,
                            shortcutHint = PREVIEW_DELETE_SHORTCUT,
                            isDestructive = true
                        )
                    )
                ),
                state = state
            ) { Text(text = PREVIEW_ROW) }
        }
    }
}
