package app.notekea.settings.domain

import app.notekea.core.settings.domain.SettingsPlace

/** A question asked beside Settings, its [reply] (whole or as written so far), and the setting it names ([place]). */
data class SettingsHelpExchange(
    val question: String,
    val reply: String,
    val place: SettingsPlace? = null
)
