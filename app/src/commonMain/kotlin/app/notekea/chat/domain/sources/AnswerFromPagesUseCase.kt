package app.notekea.chat.domain.sources

import app.notekea.chat.domain.prompt.AgentTaskPrompt
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * The answer to a question from pages clipped a moment ago for it, the text so far at each step: written from their
 * text and nothing else, since the notes' index has not read them yet.
 */
class AnswerFromPagesUseCase(private val llmEngine: LlmEngine) {
    operator fun invoke(
        question: String,
        pages: List<ClippedPage>
    ): Flow<String> = flow {
        val found =
            pages.map { "Note ${it.path.nameWithoutExtension}:\n${it.text.take(n = PAGE_CHARS)}" }

        val text = StringBuilder()

        llmEngine
            .generate(
                messages = AgentTaskPrompt.answer(
                    task = question,
                    found = found
                ),
                options = GenerationOptions(
                    maxTokens = ANSWER_TOKENS,
                    temperature = 0f
                )
            )
            .collect {
                text.append(it)
                emit(value = text.toString())
            }
    }

    private companion object {
        const val PAGE_CHARS = 4_000
        const val ANSWER_TOKENS = 800
    }
}
