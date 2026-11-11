package app.notekea.chat.domain

import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

/**
 * "How do I…" and "what can you do", answered from the app's guide ([HelpGuideSource]) with the person's shortcuts,
 * never by acting: the topics are found from the [question] and the plan's [request] (the question as it stands
 * alone), the first topic's request to try is offered, and the model explains in the question's language.
 */
class AnswerHelpUseCase(
    private val llmEngine: LlmEngine,
    private val guideSource: HelpGuideSource
) {
    operator fun invoke(
        question: String,
        request: String,
        history: List<ChatExchange>,
        shortcuts: Map<String, String>,
        language: String? = null
    ): Flow<AnswerStep> = flow {
        val guide = guideSource.load()

        val found = (
            guide.about(question = "$question $request").ifEmpty { guide.topics.take(n = 1) }
            )
            .map {
                it.copy(
                    text = HelpGuide.filled(
                        text = it.text,
                        shortcuts = shortcuts
                    )
                )
            }

        found
            .firstNotNullOfOrNull { it.tryRequest }
            ?.let { emit(value = AnswerStep.Offered(request = it)) }

        emitAll(
            flow = streamedReply(
                llmEngine = llmEngine,
                messages = HelpPrompt
                    .messages(
                        question = question,
                        topics = found,
                        history = history
                    )
                    .inLanguage(language = language),
                options = GenerationOptions(
                    maxTokens = MAX_TOKENS,
                    temperature = 0f
                )
            )
        )
    }

    private companion object {
        const val MAX_TOKENS = 350
    }
}
