package app.notekea.cards.domain

/** A card the model proposed, and what became of it ([verdict]). */
data class JudgedCard(
    val card: ProposedCard,
    val verdict: CardVerdict
)
