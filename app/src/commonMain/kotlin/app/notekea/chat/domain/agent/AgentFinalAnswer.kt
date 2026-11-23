package app.notekea.chat.domain.agent

import app.notekea.chat.domain.context.ContextBudget
import app.notekea.chat.domain.prompt.AgentTaskPrompt
import app.notekea.chat.domain.prompt.AnswerLinks
import app.notekea.core.llm.domain.GenerationOptions
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.llm.domain.LlmException
import app.notekea.core.llm.domain.ReplyText

/**
 * The answer agent mode ends a question with, written from what its moves found and nothing else (the developer's
 * smoke of 2026-11-13): shaped with Markdown, and saying what the notes do not hold instead of filling it in.
 */
internal class AgentFinalAnswer(private val llmEngine: LlmEngine) {
    /**
     * The answer to [task] from [found]; the move's own [summary] when it looked nothing up or the model fails. What
     * the person's [memory] says is true as the notes are: "my sister" is Rosa Quesada, so her phone is the sister's
     * (the loop's seventh run answered that no note says who Rosa is).
     */
    suspend fun written(
        task: String,
        found: List<String>,
        summary: String,
        memory: List<String> = emptyList()
    ): String {
        // "(search … again: its result is above)" is no result.
        val looked = found.filter { isResult(entry = it) }
        if (looked.isEmpty()) return summary

        // Every search came back empty: there is nothing to write from, and the reply is the not found one.
        val isEmptyHanded = looked.all {
            NOTHING in it
        }

        if (isEmptyHanded) return ""

        val text = try {
            ReplyText.of(
                llmEngine = llmEngine,
                messages = AgentTaskPrompt.answer(
                    task = task,
                    found = saidOf(memory = memory) + looked,
                    conclusion = summary
                ),
                options = GenerationOptions(
                    maxTokens = ANSWER_TOKENS,
                    temperature = 0f
                )
            )
        } catch (_: LlmException) {
            return summary
        }

        return AnswerLinks
            .kept(
                answer = text,
                known = (looked + task).joinToString(separator = "\n")
            )
            .ifEmpty { summary }
    }

    private fun saidOf(memory: List<String>): List<String> = if (memory.isEmpty()) {
        emptyList()
    } else {
        listOf(
            "The person's memory, what they told the chat before, as true as the notes:\n" +
                memory.joinToString(separator = "\n") { "- $it" }
        )
    }

    companion object {
        private const val AGAIN = "("

        /** Said before a result kept aside and made again, which is then shown whole once more. */
        const val SHOWN_AGAIN = "(Made again: its result was kept aside, so here it is whole.)\n"

        // What the loop says in brackets is no result, but what the budget kept aside of the dropped results and a
        // result shown again are (the bug hunt of 2026-11-17: both began with "(" and were never answered from).
        private val KEPT = listOf(ContextBudget.RECALLED, SHOWN_AGAIN)

        /** Whether [entry] of what a turn found is something it looked up, not a line of the loop's own. */
        fun isResult(entry: String): Boolean {
            val isKept = KEPT.any { entry.startsWith(prefix = it) }

            return isKept || !entry.startsWith(prefix = AGAIN)
        }

        private const val NOTHING = "nothing found"
        private const val ANSWER_TOKENS = 700
    }
}
