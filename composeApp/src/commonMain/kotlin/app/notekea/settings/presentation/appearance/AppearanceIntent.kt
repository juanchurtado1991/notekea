package app.notekea.settings.presentation.appearance

import app.notekea.core.appearance.domain.ColorTheme
import app.notekea.core.appearance.domain.ThemeMode

/** What the user chose in Settings → Appearance. */
sealed interface AppearanceIntent {
    /** A theme's card, clicked or with Enter. */
    data class OnThemeChoose(val theme: ColorTheme) : AppearanceIntent

    /** System, Light or Dark. */
    data class OnModeChoose(val mode: ThemeMode) : AppearanceIntent

    /** Reduce motion, clicked. */
    data object OnReduceMotionToggle : AppearanceIntent
}
