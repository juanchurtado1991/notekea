package app.notekea.cards.domain

/** A card asked as multiple choice: its answer among three wrong ones, in a shuffled order, at [correct]. */
data class CardChoices(
    val options: List<String>,
    val correct: Int
) {
    fun isCorrect(chosen: Int): Boolean = chosen == correct
}
