package app.notekea.chat.domain.conversation

import app.notekea.chat.domain.ChatTurn

/**
 * A vault's chat (`docs/agent-loop.md`, step 3): its [turns], oldest first, each with what it looked up, said and
 * proposed; the [summary] of the first [compacted] of them, which the model is given in their place.
 */
data class Conversation(
    val turns: List<ChatTurn> = emptyList(),
    val summary: String? = null,
    val compacted: Int = 0
) {
    /** Its last [count] turns, the summary still standing for the ones before. */
    fun last(count: Int): Conversation {
        val left = (turns.size - count).coerceAtLeast(minimumValue = 0)

        return copy(
            turns = turns.drop(n = left),
            compacted = (compacted - left).coerceAtLeast(minimumValue = 0)
        )
    }
}
