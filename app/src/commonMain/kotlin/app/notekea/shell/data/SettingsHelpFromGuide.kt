package app.notekea.shell.data

import app.notekea.chat.domain.AnswerHelpUseCase
import app.notekea.chat.domain.AnswerStep
import app.notekea.chat.domain.ChatError
import app.notekea.chat.domain.ChatExchange
import app.notekea.chat.domain.SpokenLanguage
import app.notekea.chat.presentation.HelpShortcuts
import app.notekea.core.llm.domain.LlmError
import app.notekea.core.platform.domain.PlatformConventions
import app.notekea.settings.domain.SettingsHelpError
import app.notekea.settings.domain.SettingsHelpExchange
import app.notekea.settings.domain.SettingsHelpStep
import app.notekea.settings.domain.SettingsHelper
import app.notekea.settings.domain.SettingsTopic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

/**
 * Ask about Settings, made of the chat's help ([AnswerHelpUseCase]): the guide's topic on the section open always
 * among those read, beside the ones the question names ("sign-in failed", "dark mode").
 */
class SettingsHelpFromGuide(
    private val answerHelp: AnswerHelpUseCase,
    private val conventions: PlatformConventions
) : SettingsHelper {
    override fun answer(
        question: String,
        topic: SettingsTopic,
        history: List<SettingsHelpExchange>
    ): Flow<SettingsHelpStep> {
        val earlier = history.map {
            ChatExchange(
                question = it.question,
                reply = it.reply
            )
        }

        return answerHelp(
            question = question,
            request = wordsOf(topic = topic),
            history = earlier,
            shortcuts = HelpShortcuts.of(conventions = conventions),
            language = SpokenLanguage.inChat(
                text = question,
                history = earlier
            )
        ).mapNotNull { step -> stepOf(step = step) }
    }

    private fun stepOf(step: AnswerStep): SettingsHelpStep? = when (step) {
        is AnswerStep.Writing -> SettingsHelpStep.Writing(text = step.text)
        is AnswerStep.Done -> SettingsHelpStep.Done(text = step.answer.text)
        is AnswerStep.Failed -> SettingsHelpStep.Failed(error = errorOf(error = step.error))
        else -> null
    }

    private fun errorOf(error: ChatError): SettingsHelpError = when {
        error is ChatError.Model && error.error == LlmError.NoModel -> SettingsHelpError.NoModel
        else -> SettingsHelpError.ModelFailed
    }

    // The guide's words for the section's topic, so it is read whatever the question says.
    private fun wordsOf(topic: SettingsTopic): String = when (topic) {
        SettingsTopic.APPEARANCE -> "appearance theme"
        SettingsTopic.AI_MODEL -> "ai model"
        SettingsTopic.SYNC -> "set up sync github"
    }
}
