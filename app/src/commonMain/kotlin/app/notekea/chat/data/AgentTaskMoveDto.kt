package app.notekea.chat.data

import com.ghost.serialization.annotations.GhostSerialization

/** Agent mode's move as the model writes it (4.7). */
@GhostSerialization
data class AgentTaskMoveDto(
    val move: String = "",
    val query: String? = null,
    val note: String? = null,
    val url: String? = null,
    val summary: String? = null,
    val steps: List<PlannedStepDto> = emptyList()
)
