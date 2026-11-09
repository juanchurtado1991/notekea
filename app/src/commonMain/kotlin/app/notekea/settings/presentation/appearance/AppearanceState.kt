package app.notekea.settings.presentation.appearance

import app.notekea.core.appearance.domain.Appearance

/** Settings → Appearance: the [appearance] chosen, as kept, and whether it could not be kept. */
data class AppearanceState(
    val appearance: Appearance = Appearance(),
    val hasSaveFailed: Boolean = false
)
