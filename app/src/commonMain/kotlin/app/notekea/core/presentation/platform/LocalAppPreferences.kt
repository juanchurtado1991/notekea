package app.notekea.core.presentation.platform

import androidx.compose.runtime.staticCompositionLocalOf
import app.notekea.core.preferences.domain.AppPreferences

/** The app's preferences (Settings → General and Editor), for the screens that follow them, as the editor. */
val LocalAppPreferences = staticCompositionLocalOf { AppPreferences() }
