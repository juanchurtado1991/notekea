package app.notekea.chat.data

import app.notekea.chat.domain.ChatScope
import app.notekea.chat.domain.ChatTurn
import app.notekea.chat.domain.TurnOutcome
import app.notekea.chat.domain.conversation.Conversation

/** A [Conversation] to and from what the history file holds. */
internal object ChatHistoryMappers {
    fun toDto(conversation: Conversation): ChatHistoryDto = ChatHistoryDto(
        turns = conversation.turns.map { turn ->
            ChatTurnDto(
                question = turn.question,
                scope = turn.scope.name,
                scopeName = turn.scopeName,
                outcome = turn.outcome.name,
                items = turn.items.map { TurnItemMappers.toDto(item = it) }
            )
        },
        summary = conversation.summary,
        compacted = conversation.compacted
    )

    fun toDomain(dto: ChatHistoryDto): Conversation = Conversation(
        turns = dto.turns.map { turn ->
            // A history written before the items (version 1) holds only what was said.
            val said = ChatTurn.saidOf(
                answer = turn.answer,
                sources = turn.sources.map { TurnItemMappers.sourceOf(dto = it) }
            )

            ChatTurn(
                question = turn.question,
                scope = ChatScope.entries.firstOrNull {
                    it.name == turn.scope
                } ?: ChatScope.VAULT,
                outcome = TurnOutcome.entries.firstOrNull {
                    it.name == turn.outcome
                } ?: TurnOutcome.ANSWERED,
                items = turn.items.mapNotNull { TurnItemMappers.toDomain(dto = it) } + said,
                scopeName = turn.scopeName
            )
        },
        summary = dto.summary,
        compacted = dto.compacted.coerceIn(0, dto.turns.size)
    )
}
