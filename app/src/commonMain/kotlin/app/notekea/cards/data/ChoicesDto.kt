package app.notekea.cards.data

import com.ghost.serialization.annotations.GhostSerialization

/** The wrong answers the model writes for a card's multiple choice question. */
@GhostSerialization
data class ChoicesDto(val wrong: List<String> = emptyList())
