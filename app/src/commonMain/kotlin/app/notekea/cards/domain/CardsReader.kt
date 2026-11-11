package app.notekea.cards.domain

/** Reads the model's JSON of proposed cards; null when the reply is not it. */
fun interface CardsReader {
    fun read(json: String): List<ProposedCard>?
}
