package app.notekea.cards.domain

/** A card the model proposes from a note: [question], [answer], and the note's [sentence] it comes from. */
data class ProposedCard(
    val question: String,
    val answer: String,
    val sentence: String
)
