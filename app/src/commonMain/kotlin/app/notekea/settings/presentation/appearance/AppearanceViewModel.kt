package app.notekea.settings.presentation.appearance

import app.notekea.core.appearance.domain.Appearance
import app.notekea.core.appearance.domain.AppearanceStore
import app.notekea.core.presentation.BaseViewModel
import co.touchlab.kermit.Logger

/**
 * Settings → Appearance (`ROADMAP.md` 2.12): shows the kept appearance and keeps each choice, which every window then
 * follows ([AppearanceStore.observe]).
 */
class AppearanceViewModel(
    private val appearanceStore: AppearanceStore
) : BaseViewModel<AppearanceState, AppearanceIntent, Nothing>(initialState = AppearanceState()) {
    private val logger = Logger.withTag(tag = LOG_TAG)

    init {
        launchGuarded(onError = ::logUnexpectedError) {
            appearanceStore.observe().collect { appearance -> reduce { it.copy(appearance = appearance) } }
        }
    }

    override fun handleIntent(
        intent: AppearanceIntent
    ) {
        when (intent) {
            is AppearanceIntent.OnThemeChoose -> save { it.copy(theme = intent.theme) }
            is AppearanceIntent.OnModeChoose -> save { it.copy(mode = intent.mode) }
            AppearanceIntent.OnReduceMotionToggle -> save { it.copy(reduceMotion = !it.reduceMotion) }
        }
    }

    private fun save(
        change: (Appearance) -> Appearance
    ) {
        val appearance = change(state.value.appearance)
        reduce { it.copy(appearance = appearance, hasSaveFailed = false) }
        launchGuarded(onError = ::logUnexpectedError) { appearanceStore.save(appearance = appearance) }
    }

    private fun logUnexpectedError(
        throwable: Throwable
    ) {
        logger.e(throwable = throwable) { UNEXPECTED_ERROR_MESSAGE }
        reduce { it.copy(hasSaveFailed = true) }
    }

    private companion object {
        const val LOG_TAG = "AppearanceViewModel"
        const val UNEXPECTED_ERROR_MESSAGE = "Unexpected error with the appearance"
    }
}
