package app.notekea.chat.data

import app.notekea.chat.domain.answer.AnswerSource
import app.notekea.chat.domain.conversation.ProposalOutcome
import app.notekea.chat.domain.conversation.ProposedChange
import app.notekea.chat.domain.conversation.TurnItem
import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.tool.ToolOrigin
import app.notekea.core.notes.domain.RelativePath

/** A turn's [TurnItem]s to and from what the history file holds; an item of a kind not known is left out. */
internal object TurnItemMappers {
    private const val CALL = "call"
    private const val RESULT = "result"
    private const val TEXT = "text"
    private const val PROPOSAL = "proposal"

    fun toDto(item: TurnItem): TurnItemDto = when (item) {
        is TurnItem.Call -> TurnItemDto(
            type = CALL,
            name = item.name,
            target = item.target
        )

        is TurnItem.Result -> TurnItemDto(
            type = RESULT,
            text = item.text,
            origin = item.origin.name
        )

        is TurnItem.Text -> TurnItemDto(
            type = TEXT,
            text = item.text,
            sources = item.sources.map { source ->
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

        is TurnItem.Proposal -> TurnItemDto(
            type = PROPOSAL,
            outcome = item.outcome.name,
            problem = item.problem,
            changes = item.changes.map { change ->
                ProposedChangeDto(
                    kind = change.kind.name,
                    title = change.title,
                    start = change.start,
                    notes = change.notes.map { it.value }
                )
            }
        )
    }

    fun toDomain(dto: TurnItemDto): TurnItem? = when (dto.type) {
        CALL -> TurnItem.Call(
            name = dto.name,
            target = dto.target
        )

        // Text whose origin is not known is not taken for the person's notes.
        RESULT -> TurnItem.Result(
            text = dto.text,
            origin = ToolOrigin.entries.firstOrNull {
                it.name == dto.origin
            } ?: ToolOrigin.UNTRUSTED
        )

        TEXT -> TurnItem.Text(
            text = dto.text,
            sources = dto.sources.map { sourceOf(dto = it) }
        )

        PROPOSAL -> TurnItem.Proposal(
            changes = dto.changes.mapNotNull { changeOf(dto = it) },
            outcome = ProposalOutcome.entries.firstOrNull {
                it.name == dto.outcome
            } ?: ProposalOutcome.PROPOSED,
            problem = dto.problem
        )

        else -> null
    }

    fun sourceOf(dto: AnswerSourceDto): AnswerSource = AnswerSource(
        number = dto.number,
        note = RelativePath(value = dto.note),
        headingPath = dto.headingPath,
        range = dto.first..dto.last,
        text = dto.text
    )

    private fun changeOf(dto: ProposedChangeDto): ProposedChange? {
        val kind = ActionKind.entries.firstOrNull {
            it.name == dto.kind
        } ?: return null

        return ProposedChange(
            kind = kind,
            notes = dto.notes.map { RelativePath(value = it) },
            start = dto.start,
            title = dto.title
        )
    }
}
