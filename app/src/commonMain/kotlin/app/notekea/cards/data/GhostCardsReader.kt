package app.notekea.cards.data

import app.notekea.cards.domain.CardsReader
import app.notekea.cards.domain.ProposedCard
import com.ghost.serialization.Ghost
import com.ghost.serialization.exception.GhostJsonException

/** [CardsReader] over Ghost: a reply that is not the cards' JSON is none. */
class GhostCardsReader : CardsReader {
    override fun read(json: String): List<ProposedCard>? = try {
        Ghost
            .deserialize<CardsDto>(json)
            .cards
            .map {
                ProposedCard(
                    question = it.question.trim(),
                    answer = it.answer.trim(),
                    sentence = it.sentence.trim()
                )
            }
    } catch (_: GhostJsonException) {
        null
    }
}
