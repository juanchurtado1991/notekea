package app.notekea.core.presentation.components

import androidx.compose.runtime.Immutable

/** How a tab of a [NotekeaTabStrip] is drawn: whether it [isSelected], its pane [isActive], and it [isHovered]. */
@Immutable
internal data class TabLook(
    val isSelected: Boolean,
    val isActive: Boolean,
    val isHovered: Boolean
)
