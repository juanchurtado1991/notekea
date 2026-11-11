package app.notekea.settings.presentation.help

import app.notekea.settings.domain.SettingsHelpError
import app.notekea.settings.domain.SettingsHelpExchange

/**
 * Ask about Settings, beside the dialog: the questions asked and their replies ([exchanges], the last one being
 * written while [isAnswering]), the question being typed ([draft]), and why the last one got no reply ([error]).
 */
data class SettingsHelpState(
    val exchanges: List<SettingsHelpExchange> = emptyList(),
    val draft: String = "",
    val isAnswering: Boolean = false,
    val error: SettingsHelpError? = null
) {
    val canAsk: Boolean get() = draft.isNotBlank() && !isAnswering
}
