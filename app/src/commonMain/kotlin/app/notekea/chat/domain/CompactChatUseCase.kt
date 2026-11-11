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
        // Only what fits the model: the summary so far and the latest exchanges. A long chat from an earlier session
        // overflowed it, failed, and was tried again after every reply (the Auto-Test on the real app, 2026-11-11).
        val lines = exchanges.map {
            "Person: ${it.question.take(n = REPLY_CHARS)}\nAssistant: ${it.reply.take(n = REPLY_CHARS)}\n\n"
        }

        val said = lines
            .asReversed()
            .runningFold(initial = "") { kept, exchange ->
                exchange + kept
            }
            .takeWhile {
                it.length <= MAX_INPUT_CHARS
            }
            .last()

        val conversation = summary?.let { "Summary so far: $it\n\n$said" } ?: said

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
        const val MAX_INPUT_CHARS = 6_000
        const val INSTRUCTIONS =
            "Summarize this conversation between a person and the assistant of their notes app, for the assistant " +
                "to remember it: what the person wanted, the notes, names and numbers that came up, what was " +
                "answered or done, and what is still open. At most 120 words, plain text, in the language the person " +
                "writes in. Write only the summary."
    }
}
