package app.notekea.core.presentation.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import app.notekea.core.presentation.designsystem.preview.NotekeaPreviewThemes
import app.notekea.core.presentation.designsystem.theme.NotekeaDimensions
import app.notekea.core.presentation.designsystem.theme.NotekeaIcons

private const val PREVIEW_ROW = "Callout"
private const val SHORTCUT_MIN_WIDTH_DP = 48

/** One row of a [NotekeaContextMenuArea]'s menu, with the panel its item opens beside it, driven by [submenu]. */
@Composable
internal fun NotekeaMenuRow(
    item: NotekeaMenuItem,
    onChosen: () -> Unit,
    submenu: SubmenuControl
) {
    val colors = MaterialTheme.colorScheme
    val color = if (item.isDestructive) colors.error else colors.onSurface

    val interactionSource = remember { MutableInteractionSource() }

    val isHovered by interactionSource.collectIsHoveredAsState()

    LaunchedEffect(isHovered) {
        if (isHovered) submenu.onHover()
    }

    Box {
        DropdownMenuItem(
            text = {
                MenuRowText(
                    item = item,
                    color = color
                )
            },
            onClick = {
                if (item.opensSubmenu) {
                    submenu.onHover()
                } else {
                    if (!item.keepsMenuOpen) onChosen()
                    item.onClick()
                }
            },
            modifier = menuRowHeight(item = item)
                .hoverable(interactionSource = interactionSource)
                .onPreviewKeyEvent { event ->
                    (item.opensSubmenu && event.entersSubmenu).also {
                        if (it) submenu.onEnter()
                    }
                },
            leadingIcon = leadingIconOf(
                item = item,
                color = color
            ),
            trailingIcon = trailingOf(item = item),
            enabled = item.enabled,
            interactionSource = interactionSource
        )

        val panel = item.submenu ?: item.items?.let { sections -> submenuOf(sections = sections) }

        if (submenu.isOpen && panel != null) {
            Submenu(
                hasKeyboard = submenu.hasKeyboard,
                onChosen = onChosen,
                onLeave = submenu.onLeave,
                content = panel
            )
        }
    }
}

internal val NotekeaMenuItem.opensSubmenu: Boolean
    get() = submenu != null || items != null

private val KeyEvent.entersSubmenu: Boolean
    get() = type == KeyEventType.KeyDown && (key == Key.DirectionRight || key == Key.Enter)

/** A submenu of rows ([NotekeaMenuItem.items]): the same rows as the menu, in sections separated by dividers. */
private fun submenuOf(
    sections: List<List<NotekeaMenuItem>>
): @Composable (onChosen: () -> Unit) -> Unit = { onChosen ->
    Column(modifier = Modifier.width(intrinsicSize = IntrinsicSize.Max)) {
        sections.forEachIndexed { index, items ->
            if (index > 0) NotekeaMenuDivider()

            items.forEach { item ->
                NotekeaMenuRow(
                    item = item,
                    onChosen = onChosen,
                    submenu = SubmenuControl.NONE
                )
            }
        }
    }
}

@Composable
private fun MenuRowText(
    item: NotekeaMenuItem,
    color: Color
) {
    Column {
        Text(
            text = item.label,
            style = MaterialTheme.typography.bodyMedium,
            color = color
        )

        item.supportingText?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Desktop menus have 32 dp rows; a row with a second line grows to hold it. */
private fun menuRowHeight(item: NotekeaMenuItem): Modifier = if (item.supportingText == null) {
    Modifier.height(height = NotekeaDimensions.MenuItemHeight)
} else {
    Modifier.heightIn(
        min = NotekeaDimensions.MenuItemHeight
    ).padding(vertical = NotekeaDimensions.SpacingXs)
}

/** The shortcut at the end of the row, or a chevron for an item that opens a submenu. */
private fun trailingOf(item: NotekeaMenuItem): (@Composable () -> Unit)? = when {
    item.opensSubmenu -> {
        {
            Icon(
                imageVector = NotekeaIcons.Expand,
                contentDescription = null,
                modifier = Modifier.size(size = NotekeaDimensions.IconSize),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    item.shortcutHint != null -> {
        {
            Text(
                text = item.shortcutHint,
                modifier = Modifier.widthIn(min = SHORTCUT_MIN_WIDTH_DP.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End
            )
        }
    }

    else -> null
}

/**
 * A submenu's panel, beside its row, themed like the menu. Opened with the pointer it leaves the keyboard where it was,
 * so moving on to another row closes it; opened from the keyboard ([hasKeyboard]) it takes it, on its first row, and
 * Up, Down, Left and Esc work there.
 */
@Composable
private fun Submenu(
    hasKeyboard: Boolean,
    onChosen: () -> Unit,
    onLeave: () -> Unit,
    content: @Composable (onChosen: () -> Unit) -> Unit
) {
    val position = remember { SubmenuPositionProvider() }

    Popup(
        popupPositionProvider = position,
        onDismissRequest = onLeave.takeIf { hasKeyboard },
        properties = PopupProperties(focusable = hasKeyboard)
    ) {
        val focusManager = LocalFocusManager.current

        LaunchedEffect(hasKeyboard) {
            // One frame later: its rows are not laid out before.
            withFrameNanos { }
            if (hasKeyboard) focusManager.moveFocus(focusDirection = FocusDirection.Next)
        }

        Surface(
            modifier = Modifier.onPreviewKeyEvent { event ->
                handleSubmenuKey(
                    event = event,
                    focusManager = focusManager,
                    onLeave = onLeave
                )
            },
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                width = NotekeaDimensions.BorderWidth,
                color = MaterialTheme.colorScheme.outline
            ),
            shadowElevation = NotekeaDimensions.SpacingXs
        ) {
            Box(modifier = Modifier.padding(all = NotekeaDimensions.SpacingS)) { content(onChosen) }
        }
    }
}

/** Returns whether the key moved inside the submenu or left it. */
private fun handleSubmenuKey(
    event: KeyEvent,
    focusManager: FocusManager,
    onLeave: () -> Unit
): Boolean {
    if (event.type != KeyEventType.KeyDown) return false

    when (event.key) {
        Key.DirectionDown -> focusManager.moveFocus(focusDirection = FocusDirection.Next)
        Key.DirectionUp -> focusManager.moveFocus(focusDirection = FocusDirection.Previous)
        Key.DirectionLeft, Key.Escape -> onLeave()
        else -> return false
    }

    return true
}

/** The item's icon, or for a choice its check mark (an empty slot of the same size when it is not chosen). */
private fun leadingIconOf(
    item: NotekeaMenuItem,
    color: Color
): (@Composable () -> Unit)? {
    val icon = if (item.isChecked != null) {
        NotekeaIcons.Checked.takeIf { item.isChecked }
    } else {
        item.icon
    }

    if (icon == null && item.isChecked == null) return null

    return {
        val iconModifier = Modifier.size(size = NotekeaDimensions.IconSize)

        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = iconModifier,
                tint = color
            )
        } else {
            Box(modifier = iconModifier)
        }
    }
}

/** The line between two sections of a menu. */
@Composable
internal fun NotekeaMenuDivider() {
    Box(
        modifier = Modifier
            .padding(vertical = NotekeaDimensions.SpacingXs)
            .fillMaxWidth()
            .height(height = NotekeaDimensions.BorderWidth)
            .background(color = MaterialTheme.colorScheme.outline)
    )
}

@Preview
@Composable
private fun NotekeaMenuRowPreview() {
    NotekeaPreviewThemes {
        Column(modifier = Modifier.width(intrinsicSize = IntrinsicSize.Max)) {
            NotekeaMenuRow(
                item = NotekeaMenuItem(
                    label = PREVIEW_ROW,
                    onClick = {}
                ),
                onChosen = {},
                submenu = SubmenuControl.NONE
            )
        }
    }
}

@Preview
@Composable
private fun NotekeaMenuDividerPreview() {
    NotekeaPreviewThemes {
        Column(modifier = Modifier.width(width = SHORTCUT_MIN_WIDTH_DP.dp)) { NotekeaMenuDivider() }
    }
}
