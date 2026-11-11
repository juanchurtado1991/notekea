package app.notekea.cards.data

import com.ghost.serialization.annotations.GhostSerialization

/** One card the model proposes, with the note's sentence it comes from. */
@GhostSerialization
data class CardDto(
    val question: String = "",
    val answer: String = "",
    val sentence: String = ""
)
