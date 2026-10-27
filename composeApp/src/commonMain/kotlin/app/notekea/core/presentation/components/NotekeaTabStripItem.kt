package app.notekea.core.presentation.components

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * One tab of a [NotekeaTabStrip]: what it is called ([label]), its [icon], a [key] that stays the same while open, and,
 * while it has changes not saved yet, [unsavedLabel]: a dot in place of its close button says so, named by it. [iconTint] colors the
 * icon (a file's kind) except on the active pane's selected tab.
 */
@Immutable
data class NotekeaTabStripItem(
    val key: String,
    val label: String,
    val icon: ImageVector? = null,
    val unsavedLabel: String? = null,
    val iconTint: Color? = null
)
