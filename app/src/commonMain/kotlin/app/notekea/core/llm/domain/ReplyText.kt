package app.notekea.core.llm.domain

/** A model's whole reply, its pieces joined: for a caller that reads the text only once it is complete. */
object ReplyText {
    /** [llmEngine]'s reply to [messages]; an [LlmException] reaches the caller as [LlmEngine.generate] throws it. */
    suspend fun of(
        llmEngine: LlmEngine,
        messages: List<ChatMessage>,
        options: GenerationOptions = GenerationOptions()
    ): String = buildString {
        llmEngine
            .generate(
                messages = messages,
                options = options
            )
            .collect { append(it) }
    }
}
