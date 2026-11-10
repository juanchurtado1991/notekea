package app.notekea.chat.domain

/** How a question of the chat ended. */
enum class TurnOutcome {
    ANSWERED,

    /** The notes do not answer it. */
    NOT_FOUND,

    /** Stopped by the person before the answer was whole: what was written is kept. */
    STOPPED,
    FAILED
}
