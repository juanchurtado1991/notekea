package app.notekea.chat.presentation.model

import app.notekea.chat.domain.ChatTurn
import app.notekea.chat.domain.TurnOutcome
import app.notekea.chat.domain.conversation.ProposalOutcome
import app.notekea.chat.domain.conversation.ProposedChange
import app.notekea.chat.domain.conversation.TurnItem
import app.notekea.chat.domain.plan.ActionStep

/**
 * A [ChatEntry] as a turn of the conversation, and back (`docs/agent-loop.md`, step 3): what it looked up, what it
 * proposed and how that ended, and what it said. A turn shown again is the entry of a finished turn.
 */
internal object EntryTurns {
    fun turnOf(entry: ChatEntry): ChatTurn {
        val calls = entry.moves.map {
            TurnItem.Call(
                name = it.move,
                target = it.target
            )
        }

        val proposal = entry.action?.let { proposalOf(action = it) } ?: entry.proposal

        val said = ChatTurn.saidOf(
            answer = entry.answer,
            sources = entry.sources
        )

        // Still being answered when it is kept: closed there, the app shows it as stopped.
        return ChatTurn(
            question = entry.question,
            scope = entry.scope,
            outcome = entry.outcome ?: TurnOutcome.STOPPED,
            items = calls + listOfNotNull(proposal) + said,
            scopeName = entry.scopeName
        )
    }

    fun entryOf(
        turn: ChatTurn,
        id: Long
    ): ChatEntry = ChatEntry(
        id = id,
        question = turn.question,
        scope = turn.scope,
        step = ChatStep.DONE,
        answer = turn.answer,
        sources = turn.sources,
        scopeName = turn.scopeName,
        outcome = turn.outcome,
        error = null,
        moves = turn.calls.map {
            AgentMoveLine(
                move = it.name,
                target = it.target
            )
        },
        proposal = turn.proposal
    )

    // Nothing proposed and nothing refused is no proposal: what it found or showed is all.
    private fun proposalOf(action: ChatAction): TurnItem.Proposal? {
        val isNone = action.steps.isEmpty() && action.problem == null
        if (isNone) return null

        val outcome = when (action.status) {
            ActionStatus.DONE, ActionStatus.FINISHED -> ProposalOutcome.DONE
            ActionStatus.PROPOSED, ActionStatus.RUNNING -> ProposalOutcome.PROPOSED
            ActionStatus.UNDONE -> ProposalOutcome.UNDONE
            ActionStatus.CANCELLED -> ProposalOutcome.CANCELLED
            ActionStatus.FAILED -> ProposalOutcome.FAILED
        }

        return TurnItem.Proposal(
            changes = action.steps.map { changeOf(step = it) },
            outcome = outcome,
            problem = action.problem?.toString()
        )
    }

    private fun changeOf(step: ActionStep): ProposedChange = ProposedChange(
        kind = step.kind,
        notes = step.notes,
        start = step
            .content
            .firstOrNull()
            ?.text
            .orEmpty()
            .take(n = ProposedChange.START_CHARS)
            .replace(
                oldChar = '\n',
                newChar = ' '
            ),
        title = step.title
    )
}
