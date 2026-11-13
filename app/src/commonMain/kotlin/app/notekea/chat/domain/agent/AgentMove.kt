package app.notekea.chat.domain.agent

import app.notekea.chat.domain.prompt.AgentPrompt

/** One move of the agent ([AgentAnswerUseCase]): its kind ([AgentPrompt]) and the words or the note it names. */
data class AgentMove(
    val move: String,
    val query: String? = null,
    val note: String? = null
)
