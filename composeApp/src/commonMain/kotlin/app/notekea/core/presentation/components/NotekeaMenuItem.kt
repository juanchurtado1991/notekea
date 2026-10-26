package app.notekea.core.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * One action of a [NotekeaContextMenuArea]. [shortcutHint] shows the keyboard shortcut at the end of the row, so the
 * menu teaches it; [isDestructive] colors the action in the error color, and its label must be a verb ("Delete note").
 * [isChecked] makes it one choice of a set (the tree's sort): true shows a check mark in the icon's place, false
 * keeps that space empty so the labels align; null is a plain action. [keepsMenuOpen] leaves the menu open after a
 * click, for an option of the other items (the callout menu's "Start folded"), so it can be set before choosing one.
 * [supportingText] explains in a second, smaller line what an option does when its label alone is not enough.
 * [submenu] makes the item open a panel beside the menu (on hover or click) instead of acting: the table size picker;
 * it calls the given `onChosen` once a choice is made, which closes the whole menu.
 */
data class NotekeaMenuItem(
    val label: String,
    val onClick: () -> Unit,
    val icon: ImageVector? = null,
    val shortcutHint: String? = null,
    val isDestructive: Boolean = false,
    val enabled: Boolean = true,
    val isChecked: Boolean? = null,
    val keepsMenuOpen: Boolean = false,
    val supportingText: String? = null,
    val submenu: (@Composable (onChosen: () -> Unit) -> Unit)? = null
)
