package app.notekea.core.presentation.components

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * One action of a [NotekeaContextMenuArea]. [shortcutHint] shows the keyboard shortcut at the end of the row, so the
 * menu teaches it; [isDestructive] colors the action in the error color, and its label must be a verb ("Delete note").
 */
data class NotekeaMenuItem(
    val label: String,
    val onClick: () -> Unit,
    val icon: ImageVector? = null,
    val shortcutHint: String? = null,
    val isDestructive: Boolean = false,
    val enabled: Boolean = true
)
