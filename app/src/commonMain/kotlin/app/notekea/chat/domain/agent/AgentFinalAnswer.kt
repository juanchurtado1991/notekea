package app.notekea.chat.domain.agent

import app.notekea.chat.domain.prompt.AgentTaskPrompt
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.llm.domain.LlmException

/**
 * The answer agent mode ends a question with, written from what its moves found and nothing else (the developer's
 * smoke of 2026-11-13): shaped with Markdown, and saying what the notes do not hold instead of filling it in.
 */
internal class AgentFinalAnswer(private val llmEngine: LlmEngine) {
    /** The answer to [task] from [found]; the move's own [summary] when it looked nothing up or the model fails. */
    suspend fun written(
        task: String,
        found: List<String>,
        summary: String
    ): String {
        // "(search … again: its result is above)" is no result.
        val looked = found.filterNot { it.startsWith(prefix = AGAIN) }
        if (looked.isEmpty()) return summary

        // Every search came back empty: there is nothing to write from, and the reply is the not found one.
        val isEmptyHanded = looked.all {
            NOTHING in it
        }

        if (isEmptyHanded) return ""

        val text = StringBuilder()

        try {
            llmEngine
                .generate(
                    messages = AgentTaskPrompt.answer(
                        task = task,
                        found = looked
                    ),
                    options = GenerationOptions(
                        maxTokens = ANSWER_TOKENS,
                        temperature = 0f
                    )
                )
                .collect { text.append(it) }
        } catch (_: LlmException) {
            return summary
        }

        return text
            .toString()
            .trim()
            .ifEmpty { summary }
    }

    companion object {
        const val AGAIN = "("
        private const val NOTHING = "nothing found"
        private const val ANSWER_TOKENS = 700
    }
}
