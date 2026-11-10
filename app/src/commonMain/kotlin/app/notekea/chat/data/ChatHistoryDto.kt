package app.notekea.chat.data

import com.ghost.serialization.annotations.GhostSerialization

/** A vault's chat history file, with its format's [version]. */
@GhostSerialization
data class ChatHistoryDto(
    val version: Int = 1,
    val turns: List<ChatTurnDto> = emptyList()
)
