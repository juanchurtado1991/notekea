package app.notekea.cards.data

import com.ghost.serialization.annotations.GhostSerialization

/** The cards the model proposes, as it writes them. */
@GhostSerialization
data class CardsDto(val cards: List<CardDto> = emptyList())
