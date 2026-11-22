package app.notekea.cards.domain

/**
 * A [card] as an answer scheduled it in its note, and the note's text before ([original]): an editor that has the
 * note open gives its unsaved edits the same change instead of undoing it on its next save.
 */
data class AnsweredCard(
    val card: StudyCard,
    val original: String
)
