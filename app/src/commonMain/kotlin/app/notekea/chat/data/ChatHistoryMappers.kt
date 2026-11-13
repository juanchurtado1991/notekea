package app.notekea.chat.data

import app.notekea.chat.domain.ChatScope
import app.notekea.chat.domain.ChatTurn
import app.notekea.chat.domain.TurnOutcome
import app.notekea.chat.domain.answer.AnswerSource
import app.notekea.core.notes.domain.RelativePath

/** [ChatTurn]s to and from what the history file holds. */
internal object ChatHistoryMappers {
    fun toDto(turns: List<ChatTurn>): ChatHistoryDto = ChatHistoryDto(
        turns = turns.map { turn ->
            ChatTurnDto(
                question = turn.question,
                scope = turn.scope.name,
                scopeName = turn.scopeName,
                outcome = turn.outcome.name,
                answer = turn.answer,
                sources = turn.sources.map { source ->
                    AnswerSourceDto(
                        number = source.number,
                        note = source.note.value,
                        headingPath = source.headingPath,
                        first = source.range.first,
                        last = source.range.last,
                        text = source.text
                    )
                }
            )
        }
    )

    fun toDomain(dto: ChatHistoryDto): List<ChatTurn> = dto.turns.map { turn ->
        ChatTurn(
            question = turn.question,
            scope = ChatScope.entries.firstOrNull {
                it.name == turn.scope
            } ?: ChatScope.VAULT,
            scopeName = turn.scopeName,
            outcome = TurnOutcome.entries.firstOrNull {
                it.name == turn.outcome
            } ?: TurnOutcome.ANSWERED,
            answer = turn.answer,
            sources = turn.sources.map { source ->
                AnswerSource(
                    number = source.number,
                    note = RelativePath(value = source.note),
                    headingPath = source.headingPath,
                    range = source.first..source.last,
                    text = source.text
                )
            }
        )
    }
}
