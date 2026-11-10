package app.notekea.core.llm.domain

/** One message of what the model is given to answer: its [role] and its [text]. */
data class ChatMessage(
    val role: ChatRole,
    val text: String
)
