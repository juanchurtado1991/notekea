package app.notekea.chat.data

import com.ghost.serialization.annotations.GhostSerialization

/**
 * A vault's chat history file, with its format's [version]: 2 since a turn holds its items, and the chat its
 * [summary] of the first [compacted] turns.
 */
@GhostSerialization
data class ChatHistoryDto(
    val version: Int = 2,
    val turns: List<ChatTurnDto> = emptyList(),
    val summary: String? = null,
    val compacted: Int = 0
)
