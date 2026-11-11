package app.notekea.chat.domain

/** One move of the agent ([AgentAnswerUseCase]): its kind ([AgentPrompt]) and the words or the note it names. */
data class AgentMove(
    val move: String,
    val query: String? = null,
    val note: String? = null
)
