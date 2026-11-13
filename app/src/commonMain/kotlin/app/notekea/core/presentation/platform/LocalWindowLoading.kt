package app.notekea.core.presentation.platform

import androidx.compose.runtime.staticCompositionLocalOf

/** The window's [WindowLoading], one for the window: the vault writes it, the first-run welcome reads it. */
val LocalWindowLoading = staticCompositionLocalOf { WindowLoading() }
