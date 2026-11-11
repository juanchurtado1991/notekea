package app.notekea.settings.domain

/** Where a reply beside Settings stands. */
sealed interface SettingsHelpStep {
    /** The reply so far, as the model writes it. */
    data class Writing(val text: String) : SettingsHelpStep

    data class Done(val text: String) : SettingsHelpStep

    data class Failed(val error: SettingsHelpError) : SettingsHelpStep
}
