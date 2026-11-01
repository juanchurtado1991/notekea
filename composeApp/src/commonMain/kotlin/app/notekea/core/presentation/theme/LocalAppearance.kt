package app.notekea.core.presentation.theme

import androidx.compose.runtime.staticCompositionLocalOf
import app.notekea.core.appearance.domain.Appearance

/**
 * The color theme and mode chosen in Settings → Appearance (`ROADMAP.md` 2.12), provided around the app's windows so
 * every [NotekeaTheme] follows a change at once; the default palette following the system until it is read.
 */
val LocalAppearance = staticCompositionLocalOf { Appearance() }
