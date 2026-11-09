package app.notekea.core.presentation.platform

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition

/** How the main window opens. */
data class WindowOpening(
    val placement: WindowPlacement,
    val size: DpSize,
    val position: WindowPosition
)
