package app.notekea.chat.data

import com.ghost.serialization.annotations.GhostSerialization

/** One move of the agent as the model writes it (`AgentPrompt.SCHEMA`). */
@GhostSerialization
data class AgentMoveDto(
    val move: String = "",
    val query: String? = null,
    val note: String? = null
)
