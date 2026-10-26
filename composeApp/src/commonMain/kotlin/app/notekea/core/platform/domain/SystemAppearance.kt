package app.notekea.core.platform.domain

import kotlinx.coroutines.flow.Flow

/**
 * Whether the system asks for a dark theme, where Compose cannot tell (on Linux it always answers light): true for
 * dark, false for light, null when the system does not say, so the theme falls back to Compose's own answer. It emits
 * again when the user changes the setting.
 */
interface SystemAppearance {
    fun observeDarkTheme(): Flow<Boolean?>
}
