package app.notekea.chat.data

import com.ghost.serialization.annotations.GhostSerialization

/**
 * A question of the chat as the history keeps it; enums by name, an unknown one read as the default. [items] hold
 * all that followed; [answer] and [sources] are what a history written before them holds, read once.
 */
@GhostSerialization
data class ChatTurnDto(
    val question: String,
    val scope: String? = null,
    val scopeName: String? = null,
    val outcome: String? = null,
    val answer: String = "",
    val sources: List<AnswerSourceDto> = emptyList(),
    val items: List<TurnItemDto> = emptyList()
)
