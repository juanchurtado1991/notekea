package app.notekea.chat.domain

import app.notekea.chat.domain.answer.AnswerSource
import app.notekea.chat.domain.conversation.TurnItem

/**
 * A question of the chat and all that followed: asked of [scope] (the note or folder [scopeName] names, when not the
 * vault), the [items] it looked up, said and proposed, in order, and how it ended. Kept in the vault's chat history.
 */
data class ChatTurn(
    val question: String,
    val scope: ChatScope,
    val outcome: TurnOutcome,
    val items: List<TurnItem>,
    val scopeName: String?
) {
    /** A question that was only answered: its [answer] text and the [sources] it cites as `[n]`. */
    constructor(
        question: String,
        scope: ChatScope,
        outcome: TurnOutcome,
        answer: String,
        sources: List<AnswerSource>,
        scopeName: String?
    ) : this(
        question = question,
        scope = scope,
        outcome = outcome,
        items = saidOf(
            answer = answer,
            sources = sources
        ),
        scopeName = scopeName
    )

    private val said: List<TurnItem.Text> get() = items.filterIsInstance<TurnItem.Text>()

    val answer: String get() = said.joinToString(separator = "\n") { it.text }

    val sources: List<AnswerSource> get() = said.flatMap { it.sources }

    val calls: List<TurnItem.Call> get() = items.filterIsInstance<TurnItem.Call>()

    val proposal: TurnItem.Proposal?
        get() = items
            .filterIsInstance<TurnItem.Proposal>()
            .lastOrNull()

    companion object {
        /** What a turn said as its items: none when it said nothing. */
        fun saidOf(
            answer: String,
            sources: List<AnswerSource>
        ): List<TurnItem> {
            val isSilent = answer.isBlank() && sources.isEmpty()
            if (isSilent) return emptyList()

            val text = TurnItem.Text(
                text = answer,
                sources = sources
            )

            return listOf(text)
        }
    }
}
