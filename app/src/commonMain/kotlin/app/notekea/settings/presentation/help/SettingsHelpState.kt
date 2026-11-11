package app.notekea.settings.presentation.help

import app.notekea.core.presentation.settings.SettingsSpotlight
import app.notekea.settings.domain.SettingsHelpError
import app.notekea.settings.domain.SettingsHelpExchange

/**
 * Ask about Settings, beside the dialog: the questions asked and their replies ([exchanges], the last one being
 * written while [isAnswering]), the question being typed ([draft]), why the last one got no reply ([error]), and the
 * setting to show ([spotlight]).
 */
data class SettingsHelpState(
    val exchanges: List<SettingsHelpExchange> = emptyList(),
    val draft: String = "",
    val isAnswering: Boolean = false,
    val error: SettingsHelpError? = null,
    val spotlight: SettingsSpotlight? = null
) {
    val canAsk: Boolean get() = draft.isNotBlank() && !isAnswering
}
