package app.notekea.cards.domain

/** Why no cards could be made from a note. */
enum class CardsProblem {
    NOTE_UNREADABLE,
    MODEL_FAILED,

    /** The model proposed cards, but the note backs none of them. */
    NONE_BACKED
}
