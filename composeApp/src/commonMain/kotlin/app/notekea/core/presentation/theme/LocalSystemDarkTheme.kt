package app.notekea.core.presentation.theme

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * The system's dark theme setting as [app.notekea.core.platform.domain.SystemAppearance] reads it, provided around
 * the app's windows; null when unknown, and [NotekeaTheme] then asks Compose.
 */
val LocalSystemDarkTheme = staticCompositionLocalOf<Boolean?> { null }
