package app.notekea.core.presentation.components

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

/** State shown by the dot of a [StatusBarItem] (indexing, model, sync). */
enum class StatusIndicator {
    ACTIVE,
    ERROR,
    IDLE;

    fun color(
        colorScheme: ColorScheme
    ): Color = when (this) {
        ACTIVE -> colorScheme.primary
        ERROR -> colorScheme.error
        IDLE -> colorScheme.onSurfaceVariant
    }
}
