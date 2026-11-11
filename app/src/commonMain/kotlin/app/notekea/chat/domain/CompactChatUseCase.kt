package app.notekea.chat.domain

import app.notekea.core.domain.Outcome
import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.ChatRole
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.llm.domain.LlmError
import app.notekea.core.llm.domain.LlmException

/**
 * The chat compacted, as Claude Code's /compact: what was said before the last turns, the [summary] kept so far and
 * the [exchanges] since, written down short, so a later follow-up still knows what the person was after.
 */
class CompactChatUseCase(private val llmEngine: LlmEngine) {
    suspend operator fun invoke(
        exchanges: List<ChatExchange>,
        summary: String?
    ): Outcome<String, LlmError> {
        val conversation = buildString {
            summary?.let { append("Summary so far: ").append(it).append("\n\n") }

            exchanges.forEach {
                append("Person: ").append(it.question).append('\n')
                append("Assistant: ").append(it.reply.take(n = REPLY_CHARS)).append("\n\n")
            }
        }

        val written = StringBuilder()

        try {
            llmEngine
                .generate(
                    messages = listOf(
                        ChatMessage(
                            role = ChatRole.SYSTEM,
                            text = INSTRUCTIONS
                        ),
                        ChatMessage(
                            role = ChatRole.USER,
                            text = conversation.trim()
                        )
                    ),
                    options = GenerationOptions(
                        maxTokens = MAX_TOKENS,
                        temperature = 0f
                    )
                )
                .collect { written.append(it) }
        } catch (exception: LlmException) {
            return Outcome.Failure(error = exception.error)
        }

        return Outcome.Success(value = written.toString().trim())
    }

    private companion object {
        const val MAX_TOKENS = 260
        const val REPLY_CHARS = 600
        const val INSTRUCTIONS =
            "Summarize this conversation between a person and the assistant of their notes app, for the assistant " +
                "to remember it: what the person wanted, the notes, names and numbers that came up, what was " +
                "answered or done, and what is still open. At most 120 words, plain text, in the language the person " +
                "writes in. Write only the summary."
    }
}
