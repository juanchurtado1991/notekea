package app.notekea.core.llm.domain

/**
 * What the chat shows of the model in use: which one it is ([tier]; the small one's answers ask to check the
 * sources), and the tokens one call may use ([contextTokens]).
 */
data class LlmModelInfo(
    val isSmall: Boolean,
    val tier: ModelTier? = null,
    // The tokens one call may use, its prompt and its reply together, when no other call runs beside it.
    val contextTokens: Int = DEFAULT_CONTEXT_TOKENS
) {
    companion object {
        const val DEFAULT_CONTEXT_TOKENS = 4_096
    }
}
