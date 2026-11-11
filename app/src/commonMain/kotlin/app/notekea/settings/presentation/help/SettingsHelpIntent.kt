package app.notekea.settings.presentation.help

import app.notekea.settings.domain.SettingsTopic

sealed interface SettingsHelpIntent {
    data class OnDraftChange(val text: String) : SettingsHelpIntent

    /** The question typed is asked about the section open ([topic]); its reply is written under it. */
    data class OnAsk(val topic: SettingsTopic) : SettingsHelpIntent

    /** The reply being written stops where it is. */
    data object OnStop : SettingsHelpIntent
}
