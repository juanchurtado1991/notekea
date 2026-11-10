package app.notekea.core.llm.domain

/** What the chat shows of the model in use: whether it is the small one, whose answers ask to check the sources. */
data class LlmModelInfo(val isSmall: Boolean)
