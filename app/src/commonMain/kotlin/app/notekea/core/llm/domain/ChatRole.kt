package app.notekea.core.llm.domain

/** Who says a [ChatMessage]: the instructions, the person, or the model. */
enum class ChatRole {
    SYSTEM,
    USER,
    ASSISTANT
}
