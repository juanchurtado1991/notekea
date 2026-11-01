package app.notekea.core.presentation.settings

import app.notekea.core.appearance.domain.Appearance

/** Settings → Appearance (`ROADMAP.md` 2.12): the [appearance] chosen, as kept. */
data class AppearanceState(
    val appearance: Appearance = Appearance()
)
