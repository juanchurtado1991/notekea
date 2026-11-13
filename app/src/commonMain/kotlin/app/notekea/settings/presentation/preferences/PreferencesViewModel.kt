package app.notekea.settings.presentation.preferences

import app.notekea.core.preferences.domain.AppPreferences
import app.notekea.core.preferences.domain.AppPreferencesStore
import app.notekea.core.presentation.BaseViewModel
import co.touchlab.kermit.Logger

/**
 * Settings → General and Editor (5.2) on the app's [store]: each change is saved at once, and every screen that reads
 * the preferences (the editor, the welcome) follows it.
 */
class PreferencesViewModel(
    private val store: AppPreferencesStore
) : BaseViewModel<AppPreferences, PreferencesIntent, Nothing>(initialState = AppPreferences()) {
    private val logger = Logger.withTag(tag = LOG_TAG)

    init {
        launchGuarded(onError = ::logError) {
            store.observe().collect { saved ->
                reduce { saved }
            }
        }
    }

    override fun handleIntent(intent: PreferencesIntent) {
        when (intent) {
            is PreferencesIntent.OnLanguageChoose -> save { it.copy(language = intent.language) }

            PreferencesIntent.OnOpenLastVaultToggle -> save {
                it.copy(opensLastVault = !it.opensLastVault)
            }

            PreferencesIntent.OnShowWelcomeAgain -> save { it.copy(hasSeenWelcome = false) }

            is PreferencesIntent.OnWebUpdateChoose -> save { it.copy(webUpdate = intent.every) }

            is PreferencesIntent.OnTextSizeChoose -> save { it.copy(textSize = intent.size) }

            is PreferencesIntent.OnLineWidthChoose -> save { it.copy(lineWidth = intent.width) }
        }
    }

    private fun save(change: (AppPreferences) -> AppPreferences) {
        val changed = change(state.value)
        reduce { changed }
        launchGuarded(onError = ::logError) { store.save(preferences = changed) }
    }

    private fun logError(throwable: Throwable) {
        logger.w(throwable = throwable) { FAILED_MESSAGE }
    }

    private companion object {
        const val LOG_TAG = "PreferencesViewModel"
        const val FAILED_MESSAGE = "Preferences could not be saved"
    }
}
