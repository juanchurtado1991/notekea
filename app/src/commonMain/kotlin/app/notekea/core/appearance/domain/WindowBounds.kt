package app.notekea.core.appearance.domain

/**
 * Where the main window was left: its size and position while not maximized (null when it had none yet) and whether it
 * was maximized, so a maximized window still goes back to its own size.
 */
data class WindowBounds(
    val widthDp: Int,
    val heightDp: Int,
    val isMaximized: Boolean,
    val xDp: Int?,
    val yDp: Int?
)
