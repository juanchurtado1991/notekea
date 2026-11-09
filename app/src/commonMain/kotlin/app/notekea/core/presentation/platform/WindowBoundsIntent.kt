package app.notekea.core.presentation.platform

import app.notekea.core.appearance.domain.WindowBounds

/** What happened to the main window. */
sealed interface WindowBoundsIntent {
    /** It was moved or resized, not maximized: [bounds] are its own size and position. */
    data class OnFloating(val bounds: WindowBounds) : WindowBoundsIntent

    /** It was maximized: its size is the screen's now, so the one it goes back to stays as kept. */
    data object OnMaximized : WindowBoundsIntent
}
