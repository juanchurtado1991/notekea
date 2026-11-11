package app.notekea.settings.domain

/** A question asked beside Settings and its [reply], whole or as written so far. */
data class SettingsHelpExchange(
    val question: String,
    val reply: String
)
