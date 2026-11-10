package app.notekea.core.llm.domain

/**
 * What the chat shows of the model in use: whether it is the small one, whose answers ask to check the sources, and
 * whether Deep answer is offered, only for the sizes the evaluation shows it helps (3.6).
 */
data class LlmModelInfo(
    val isSmall: Boolean,
    val hasDeepAnswer: Boolean = false
)
