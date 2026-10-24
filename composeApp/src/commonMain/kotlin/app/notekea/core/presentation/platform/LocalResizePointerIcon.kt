package app.notekea.core.presentation.platform

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.input.pointer.PointerIcon

/**
 * The pointer shown over a horizontal resize handle. Compose has no resize cursor in common code, so the desktop
 * window provides the system one; previews and tests fall back to the hand.
 */
val LocalResizePointerIcon = staticCompositionLocalOf { PointerIcon.Hand }
