package app.notekea.core.appearance.domain

/**
 * How the app looks (Settings → Appearance): its [theme], its [mode], whether to [reduceMotion], and whether it
 * [usesSystemAccent], the system's accent color in place of the theme's (macOS and Windows, redesign 4B).
 */
data class Appearance(
    val theme: ColorTheme = ColorTheme.DEFAULT,
    val mode: ThemeMode = ThemeMode.SYSTEM,
    val reduceMotion: Boolean = false,
    val usesSystemAccent: Boolean = false
)
