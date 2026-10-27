package app.notekea.core.presentation.components

import androidx.compose.runtime.Immutable

/**
 * What every tab of a [NotekeaTabStrip] needs to draw itself: the [selectedIndex], whether its pane [isActive], and the
 * close button's [closeLabel].
 */
@Immutable
internal data class StripLook(
    val selectedIndex: Int,
    val isActive: Boolean,
    val closeLabel: String
)
