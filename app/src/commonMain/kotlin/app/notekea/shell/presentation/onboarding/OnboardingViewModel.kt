package app.notekea.shell.presentation.onboarding

import app.notekea.core.preferences.domain.AppPreferencesStore
import app.notekea.core.presentation.BaseViewModel
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first

/**
 * The first-run welcome (5.1, the developer's design of 2026-11-11): shown at startup until it is finished or skipped,
 * which [store] keeps, or again after Settings → General's Show again; its three steps go forward and back, and the
 * notes step moves on by itself once a vault opens.
 */
class OnboardingViewModel(
    private val store: AppPreferencesStore
) : BaseViewModel<OnboardingState, OnboardingIntent, Nothing>(initialState = OnboardingState()) {
    private val logger = Logger.withTag(tag = LOG_TAG)

    init {
        launchGuarded(onError = ::logError) {
            val hasSeen = store
                .observe()
                .first()
                .hasSeenWelcome

            reduce { it.copy(isShown = !hasSeen) }
        }
    }

    override fun handleIntent(intent: OnboardingIntent) {
        when (intent) {
            OnboardingIntent.OnContinue -> move(by = 1)

            OnboardingIntent.OnBack -> move(by = -1)

            OnboardingIntent.OnWait -> reduce { it.copy(isWaiting = true) }

            OnboardingIntent.OnVaultOpened -> reduce {
                val next = if (it.step == OnboardingStep.NOTES) OnboardingStep.AI else it.step

                it.copy(
                    step = next,
                    isWaiting = false
                )
            }

            OnboardingIntent.OnFinish -> finish()
        }
    }

    private fun move(by: Int) {
        reduce {
            val index = (it.step.ordinal + by).coerceIn(0, OnboardingStep.entries.lastIndex)
            it.copy(step = OnboardingStep.entries[index])
        }
    }

    private fun finish() {
        reduce { it.copy(isShown = false) }

        launchGuarded(onError = ::logError) {
            val saved = store.observe().first()
            store.save(preferences = saved.copy(hasSeenWelcome = true))
        }
    }

    private fun logError(throwable: Throwable) {
        logger.w(throwable = throwable) { FAILED_MESSAGE }
    }

    private companion object {
        const val LOG_TAG = "OnboardingViewModel"
        const val FAILED_MESSAGE = "The welcome could not be kept as seen"
    }
}
