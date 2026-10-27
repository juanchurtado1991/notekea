package app.notekea.core.presentation.components

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * One button of a [NotekeaToolRail]: its [icon], the [label] its tooltip says, the [shortcutHint] shown beside it, what
 * a click does, and whether it [isSelected] (the panel it shows is open).
 */
@Immutable
data class NotekeaToolRailItem(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit,
    val shortcutHint: String? = null,
    val isSelected: Boolean = false
)
