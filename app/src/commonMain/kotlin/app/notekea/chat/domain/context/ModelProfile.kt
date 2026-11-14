package app.notekea.chat.domain.context

import app.notekea.core.domain.Outcome
import app.notekea.core.llm.domain.LlmEngine
import app.notekea.core.llm.domain.LlmModelInfo

/**
 * What the model in use allows the chat's loop (`docs/agent-loop.md`): the tokens one call may take when it runs
 * alone ([contextTokens]) and how many of them a turn's prompt is given ([loopTokens]). The loop reads this and
 * nothing else knows the model's limits: a larger model is another profile, not another design.
 */
data class ModelProfile(
    val contextTokens: Int,
    val loopTokens: Int
) {
    companion object {
        // Reading takes time, about 1,100 characters a second on Apple silicon (step 0): a prompt is held to what
        // is read in some seconds even when the context has room for twice as much.
        private const val MOST_LOOP_TOKENS = 8_192

        fun of(info: LlmModelInfo?): ModelProfile {
            val context = info?.contextTokens ?: LlmModelInfo.DEFAULT_CONTEXT_TOKENS

            return ModelProfile(
                contextTokens = context,
                loopTokens = context.coerceAtMost(maximumValue = MOST_LOOP_TOKENS)
            )
        }

        /** The profile of the model [llmEngine] has in use now; the narrowest one when it has none. */
        suspend fun of(llmEngine: LlmEngine): ModelProfile =
            of(info = (llmEngine.readiness() as? Outcome.Success)?.value)
    }
}
