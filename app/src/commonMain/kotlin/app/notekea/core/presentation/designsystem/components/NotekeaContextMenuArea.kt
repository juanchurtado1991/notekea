package app.notekea.core.presentation.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons

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

/**
 * Wraps [content] with a context menu of [sections] (separated by dividers) that opens at the pointer on a right
 * click, or at its top start on Shift+F10 or the Menu key while the keyboard is inside it, as anywhere its [state] is
 * opened from the keyboard. Areas can nest: a right click or those keys open only the innermost menu. [onOpenByPointer] runs on that right click, so the clicked item can be selected first, as file managers do.
 * Choosing an action closes the menu first, unless it [NotekeaMenuItem.keepsMenuOpen]. With [takesFocus] false a menu
 * opened with the pointer does not take the keyboard, so a text field keeps its selection while it is open. In a menu
 * that has it, Right or Enter goes into a row's submenu and Left or Esc comes back. The menu is themed like the rest of
 * the app (surface, outline border, 6 dp corners) on every platform, with the 32 dp rows of desktop menus and shortcuts
 * aligned at the end.
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
    NotekeaContextMenuArea(
        menu = { sections },
        modifier = modifier,
        state = state,
        onOpenByPointer = onOpenByPointer,
        takesFocus = takesFocus,
        content = content
    )
}

/** A [NotekeaContextMenuArea] whose sections are built ([menu]) only while it is open, for lists of many rows. */
@Composable
fun NotekeaContextMenuArea(
    menu: @Composable () -> List<List<NotekeaMenuItem>>,
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
            .onKeyEvent { event ->
                event.isContextMenuKey.also { if (it) state.open(byKeyboard = true) }
            }
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
            properties = PopupProperties(focusable = takesFocus || state.isByKeyboard),
            shape = MaterialTheme.shapes.small,
            containerColor = MaterialTheme.colorScheme.surface,
            border = BorderStroke(width = NotekeaDimensions.BorderWidth, color = MaterialTheme.colorScheme.outline)
        ) {
            MenuRows(sections = menu(), onChosen = state::close)
        }
    }
}

/** The rows of the menu's [sections], one submenu open at most. */
@Composable
private fun MenuRows(
    sections: List<List<NotekeaMenuItem>>,
    onChosen: () -> Unit
) {
    // The label of the item whose submenu is open; hovering another item closes it, moving into the submenu does
    // not. A label, not the item: a choice in the submenu ("Start folded") rebuilds the items.
    var openSubmenu by remember { mutableStateOf<String?>(value = null) }
    var hasKeyboard by remember { mutableStateOf(value = false) }
    sections.forEachIndexed { index, items ->
        if (index > 0) NotekeaMenuDivider()
        items.forEach { item ->
            NotekeaMenuRow(
                item = item,
                onChosen = onChosen,
                submenu = SubmenuControl(
                    isOpen = openSubmenu == item.label,
                    hasKeyboard = hasKeyboard,
                    onHover = {
                        openSubmenu = item.label.takeIf { item.opensSubmenu }
                        hasKeyboard = false
                    },
                    onEnter = {
                        openSubmenu = item.label
                        hasKeyboard = true
                    },
                    onLeave = {
                        openSubmenu = null
                        hasKeyboard = false
                    }
                )
            )
        }
    }
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
