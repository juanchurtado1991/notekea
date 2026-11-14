package app.notekea.chat.domain.conversation

/** How a proposal of the chat ended. */
enum class ProposalOutcome {
    /** On its card, neither confirmed nor cancelled. */
    PROPOSED,
    DONE,
    CANCELLED,

    /** Done, then taken back with Undo. */
    UNDONE,
    FAILED
}
