package app.notekea.settings.domain

import app.notekea.core.settings.domain.SettingsTopic
import kotlinx.coroutines.flow.Flow

/**
 * Ask about Settings (the developer's request of 2026-11-11: what each setting is, and how to set it up): a
 * [question] answered from the app's guide by the chat model, about the section open ([topic]), in the question's
 * language, with the earlier questions and replies ([history]) so a follow-up knows what "it" was.
 */
fun interface SettingsHelper {
    fun answer(
        question: String,
        topic: SettingsTopic,
        history: List<SettingsHelpExchange>
    ): Flow<SettingsHelpStep>
}
