package app.notekea.core.presentation.platform

import app.notekea.core.appearance.domain.WindowBounds

/** The main window's [bounds] as last kept; null before it was ever kept. */
data class WindowBoundsState(val bounds: WindowBounds? = null)
