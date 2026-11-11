package app.notekea.core.llm.domain

/**
 * What the chat shows of the model in use: which one it is ([tier]; the small one's answers ask to check the sources),
 * and whether Deep thinking (3.6) and agent mode (4.7) are offered, only for the sizes the evaluations show they help.
 */
data class LlmModelInfo(
    val isSmall: Boolean,
    val tier: ModelTier? = null,
    val hasDeepAnswer: Boolean = false,
    val hasAgent: Boolean = false
)
