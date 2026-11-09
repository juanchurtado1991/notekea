package app.notekea.core.presentation.platform

import androidx.compose.runtime.staticCompositionLocalOf

/** The window's [ShortcutDispatcher]; the default instance receives no events, so previews and tests need no window. */
val LocalShortcutDispatcher = staticCompositionLocalOf { ShortcutDispatcher() }
