package app.notekea.settings.presentation.preferences

import app.notekea.core.preferences.domain.AppLanguage
import app.notekea.core.preferences.domain.LineWidth
import app.notekea.core.preferences.domain.WebUpdateEvery

/** What a person changes in Settings → General and Editor. */
sealed interface PreferencesIntent {
    data class OnLanguageChoose(val language: AppLanguage) : PreferencesIntent

    data object OnOpenLastVaultToggle : PreferencesIntent

    /** Show again: the first-run welcome comes back the next time Settings closes. */
    data object OnShowWelcomeAgain : PreferencesIntent

    /** How often the web sources are updated on their own. */
    data class OnWebUpdateChoose(val every: WebUpdateEvery) : PreferencesIntent

    data class OnTextSizeChoose(val size: Int) : PreferencesIntent

    data class OnLineWidthChoose(val width: LineWidth) : PreferencesIntent
}
