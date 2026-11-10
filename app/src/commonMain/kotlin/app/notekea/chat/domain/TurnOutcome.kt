package app.notekea.chat.domain

/** How a question of the chat ended. */
enum class TurnOutcome {
    ANSWERED,

    /** The notes do not answer it. */
    NOT_FOUND,

    /** Stopped by the person before the answer was whole: what was written is kept. */
    STOPPED,
    FAILED,

    /** A request the chat acted on (3.9): what it did is in the chat while it is open, not in the history. */
    ACTED
}
