package app.notekea.core.presentation.platform

import androidx.compose.runtime.Immutable
import app.notekea.core.appearance.domain.UiScale

/**
 * The window's zoom as the screens see it: its [scale], [onReset] back to the system's size, and [resetHint], the
 * shortcut that does the same, so the status bar can show the zoom while it is not 100 % and undo it with a click.
 */
@Immutable
data class UiZoomState(
    val scale: Float = UiScale.DEFAULT,
    val onReset: () -> Unit = {},
    val resetHint: String = ""
) {
    /** Whether the window is drawn at another size than the system's. */
    val isZoomed: Boolean get() = scale != UiScale.DEFAULT
}
