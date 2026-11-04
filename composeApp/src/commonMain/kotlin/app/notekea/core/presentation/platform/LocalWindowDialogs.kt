package app.notekea.core.presentation.platform

import androidx.compose.runtime.staticCompositionLocalOf

/** The window's [WindowDialogs]; the default instance is drawn by nothing, so previews and tests need no window. */
val LocalWindowDialogs = staticCompositionLocalOf { WindowDialogs() }
