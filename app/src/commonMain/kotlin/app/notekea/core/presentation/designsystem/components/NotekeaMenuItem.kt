package app.notekea.core.presentation.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * One action of a [NotekeaContextMenuArea]. [isChecked] makes it one choice of a set (null is a plain action);
 * [keepsMenuOpen] leaves the menu open after a click; [submenu] opens a panel beside the menu instead of acting, which
 * calls its `onChosen` once a choice is made, and [items] does the same with menu rows.
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
    val submenu: (@Composable (onChosen: () -> Unit) -> Unit)? = null,
    val items: List<List<NotekeaMenuItem>>? = null
)
