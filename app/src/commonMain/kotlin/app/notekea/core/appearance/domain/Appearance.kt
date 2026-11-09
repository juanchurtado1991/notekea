package app.notekea.core.appearance.domain

/** How the app looks (Settings → Appearance): its [theme], its [mode] and whether to [reduceMotion]. */
data class Appearance(
    val theme: ColorTheme = ColorTheme.DEFAULT,
    val mode: ThemeMode = ThemeMode.SYSTEM,
    val reduceMotion: Boolean = false
)
