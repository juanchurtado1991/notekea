package app.notekea.cards.domain

/** What became of a card the model proposed (4.6): kept, or the first check it failed. */
enum class CardVerdict {
    /** Its question or its answer is empty. */
    BLANK,

    /** The note already has it as a card. */
    KNOWN,

    /** Its sentence is not the note's: too short, or not in the note as written. */
    UNQUOTED,

    /** The reranker finds that its sentence does not back the question and its answer. */
    UNBACKED,

    /** A card kept before it asks the same, or more than a note's cards were made. */
    REPEATED,

    KEPT
}
