package app.notekea.core.presentation.platform

import androidx.compose.runtime.staticCompositionLocalOf
import app.notekea.core.notes.domain.RelativePath

/**
 * Updates the web sources under a path from their pages (the developer's request of 2026-11-13): what the menu of the
 * Web sources folder, and of what it holds, offers. The window gives it; where there is none the menu has no such item.
 */
val LocalWebSourceUpdater = staticCompositionLocalOf<((RelativePath) -> Unit)?> { null }
