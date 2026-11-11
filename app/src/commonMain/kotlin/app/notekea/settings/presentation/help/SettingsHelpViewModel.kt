package app.notekea.settings.presentation.help

import app.notekea.core.presentation.BaseViewModel
import app.notekea.settings.domain.SettingsHelpExchange
import app.notekea.settings.domain.SettingsHelpStep
import app.notekea.settings.domain.SettingsHelper
import app.notekea.settings.domain.SettingsTopic
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Job

/**
 * Ask about Settings: a question is answered by the [helper] about the section open, as it is written, with the
 * earlier ones; Stop ends the reply where it is. One conversation for the whole dialog, whatever section is open.
 */
class SettingsHelpViewModel(
    private val helper: SettingsHelper
) : BaseViewModel<SettingsHelpState, SettingsHelpIntent, Nothing>(
    initialState = SettingsHelpState()
) {
    private val logger = Logger.withTag(tag = LOG_TAG)
    private var answer: Job? = null

    override fun handleIntent(intent: SettingsHelpIntent) {
        when (intent) {
            is SettingsHelpIntent.OnDraftChange -> reduce { it.copy(draft = intent.text) }
            is SettingsHelpIntent.OnAsk -> ask(topic = intent.topic)
            SettingsHelpIntent.OnStop -> stop()
        }
    }

    private fun ask(topic: SettingsTopic) {
        val current = state.value
        if (!current.canAsk) return
        val question = current.draft.trim()
        val history = current.exchanges

        reduce {
            it.copy(
                exchanges = history + SettingsHelpExchange(
                    question = question,
                    reply = ""
                ),
                draft = "",
                isAnswering = true,
                error = null
            )
        }

        answer = launchGuarded(onError = ::failed) {
            helper
                .answer(
                    question = question,
                    topic = topic,
                    history = history
                )
                .collect { step -> show(step = step) }

            reduce { it.copy(isAnswering = false) }
        }
    }

    private fun show(step: SettingsHelpStep) {
        when (step) {
            is SettingsHelpStep.Writing -> written(text = step.text)
            is SettingsHelpStep.Done -> written(text = step.text)
            is SettingsHelpStep.Failed -> reduce { it.copy(error = step.error) }
        }
    }

    private fun written(text: String) {
        reduce {
            it.copy(
                exchanges = it.exchanges.dropLast(n = 1) + it
                    .exchanges
                    .last()
                    .copy(reply = text)
            )
        }
    }

    private fun stop() {
        answer?.cancel()
        reduce { it.copy(isAnswering = false) }
    }

    private fun failed(throwable: Throwable) {
        logger.w(throwable = throwable) { FAILED_MESSAGE }
        reduce { it.copy(isAnswering = false) }
    }

    private companion object {
        const val LOG_TAG = "SettingsHelpViewModel"
        const val FAILED_MESSAGE = "Sync help failed"
    }
}
