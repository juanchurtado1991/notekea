package app.notekea.core.presentation.platform

import androidx.compose.runtime.staticCompositionLocalOf

/** The window's [WindowCloseGuard]; previews and tests get one that nothing calls. */
val LocalWindowCloseGuard = staticCompositionLocalOf { WindowCloseGuard() }
