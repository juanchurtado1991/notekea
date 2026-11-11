package app.notekea.core.presentation.settings

import app.notekea.core.settings.domain.SettingsPlace

/** A setting to show the person: its [place], and a [serial] that grows each time, so showing it again blinks again. */
data class SettingsSpotlight(
    val place: SettingsPlace,
    val serial: Int
) {
    /** The [serial] when [place] is this one's, else null: what `NotekeaSpotlight` blinks on. */
    fun serialFor(place: SettingsPlace): Int? = serial.takeIf {
        this.place == place
    }
}
