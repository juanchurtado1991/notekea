package app.notekea.core.presentation.components

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

/** One tab of a [NotekeaTabStrip]: what it is called ([label]), its [icon], and a [key] that stays the same while open. */
@Immutable
data class NotekeaTabStripItem(
    val key: String,
    val label: String,
    val icon: ImageVector? = null
)
