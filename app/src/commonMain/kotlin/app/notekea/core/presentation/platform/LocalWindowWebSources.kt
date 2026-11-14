package app.notekea.core.presentation.platform

import androidx.compose.runtime.staticCompositionLocalOf

/** The window's [WindowWebSources], one for the window: the vault writes it, Settings → General reads it. */
val LocalWindowWebSources = staticCompositionLocalOf { WindowWebSources() }
