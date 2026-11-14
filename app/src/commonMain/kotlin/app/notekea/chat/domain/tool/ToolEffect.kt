package app.notekea.chat.domain.tool

/** What a tool does to the turn: nothing in the vault changes but through a proposal the person confirms. */
enum class ToolEffect {
    /** It looks something up, at once. */
    READS,

    /** It adds a change to the proposal on the card; nothing changes until Do it. */
    PROPOSES,

    /** It ends the turn: an answer, the proposal, or a question back. */
    ENDS
}
