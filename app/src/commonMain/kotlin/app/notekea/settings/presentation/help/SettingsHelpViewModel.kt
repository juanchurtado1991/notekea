package app.notekea.settings.presentation.help

import app.notekea.core.presentation.BaseViewModel
import app.notekea.core.presentation.settings.SettingsSpotlight
import app.notekea.core.settings.domain.SettingsPlace
import app.notekea.core.settings.domain.SettingsPlaces
import app.notekea.core.settings.domain.SettingsTopic
import app.notekea.settings.domain.SettingsHelpExchange
import app.notekea.settings.domain.SettingsHelpSource
import app.notekea.settings.domain.SettingsHelpStep
import co.touchlab.kermit.Logger
import kotlinx.coroutines.Job

/**
 * Ask about Settings: a question is answered by the [helper] about the section open, with the earlier ones, and the
 * setting it names (or else its reply names) is shown at once, as an intent opens a screen; Show shows it again.
 */
class SettingsHelpViewModel(
    private val helper: SettingsHelpSource
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
            is SettingsHelpIntent.OnShow -> spotlight(place = intent.place)
        }
    }

    private fun ask(topic: SettingsTopic) {
        val current = state.value
        if (!current.canAsk) return
        val question = current.draft.trim()
        val history = current.exchanges

        // Named in the question, it is shown before the reply, even with no model to write one.
        val asked = SettingsPlaces.named(
            text = question,
            topic = topic
        )

        reduce {
            it.copy(
                exchanges = history + SettingsHelpExchange(
                    question = question,
                    reply = "",
                    place = asked
                ),
                draft = "",
                isAnswering = true,
                error = null
            )
        }

        asked?.let { spotlight(place = it) }

        answer = launchGuarded(onError = ::failed) {
            helper
                .answer(
                    question = question,
                    topic = topic,
                    history = history
                )
                .collect { step -> show(step = step) }

            reduce { it.copy(isAnswering = false) }
            if (asked == null) shownByReply(topic = topic)
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

    // Not named in the question, the setting the reply names is shown once it is written.
    private fun shownByReply(topic: SettingsTopic) {
        val last = state
            .value
            .exchanges
            .lastOrNull() ?: return

        val named = SettingsPlaces.named(
            text = last.reply,
            topic = topic
        ) ?: return

        reduce { it.copy(exchanges = it.exchanges.dropLast(n = 1) + last.copy(place = named)) }

        spotlight(place = named)
    }

    private fun spotlight(place: SettingsPlace) {
        reduce {
            it.copy(
                spotlight = SettingsSpotlight(
                    place = place,
                    serial = (it.spotlight?.serial ?: 0) + 1
                )
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
        const val FAILED_MESSAGE = "Settings help failed"
    }
}
