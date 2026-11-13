package app.notekea.chat.presentation.model

/** Where an action turn stands. */
enum class ActionStatus {
    /** The steps that change notes wait for the card's Do it or Cancel. */
    PROPOSED,
    RUNNING,
    DONE,

    /** Done, then taken back with Undo. */
    UNDONE,
    CANCELLED,

    /** Nothing to confirm: what it found or showed is all. */
    FINISHED,
    FAILED
}
