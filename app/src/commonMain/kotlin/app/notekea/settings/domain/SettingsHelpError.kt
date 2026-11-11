package app.notekea.settings.domain

/** Why Ask about Settings could not answer. */
sealed interface SettingsHelpError {
    /** No chat model is in use (Settings → AI model); Sync's steps are still shown. */
    data object NoModel : SettingsHelpError

    /** The model could not run: too little memory, or it failed. */
    data object ModelFailed : SettingsHelpError
}
