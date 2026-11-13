package app.notekea.chat.domain.answer

import app.notekea.chat.domain.language.inLanguage
import app.notekea.chat.domain.prompt.OverviewPrompt
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.notes.domain.NoteEntry
import kotlinx.coroutines.flow.Flow

/** "What is in my notes?": the vault described from its map ([OverviewPrompt]), in the question's language. */
class DescribeVaultUseCase(private val llmEngine: LlmEngine) {
    operator fun invoke(
        question: String,
        notes: Collection<NoteEntry>,
        language: String? = null
    ): Flow<AnswerStep> = streamedReply(
        llmEngine = llmEngine,
        messages = OverviewPrompt
            .messages(
                question = question,
                notes = notes
            )
            .inLanguage(language = language),
        options = GenerationOptions(
            maxTokens = MAX_TOKENS,
            temperature = 0f
        )
    )

    private companion object {
        const val MAX_TOKENS = 350
    }
}
