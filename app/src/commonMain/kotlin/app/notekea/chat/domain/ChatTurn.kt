package app.notekea.chat.domain

/**
 * A question of the chat and how it ended: asked of [scope] (the note or folder [scopeName] names, when not the
 * vault), its [answer] text and the [sources] it cites as `[n]`, kept in the vault's chat history.
 */
data class ChatTurn(
    val question: String,
    val scope: ChatScope,
    val outcome: TurnOutcome,
    val answer: String,
    val sources: List<AnswerSource>,
    val scopeName: String?
)
