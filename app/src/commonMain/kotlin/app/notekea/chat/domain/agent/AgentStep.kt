package app.notekea.chat.domain.agent

import app.notekea.chat.domain.plan.ActionProblem
import app.notekea.chat.domain.plan.ActionStep

/** Where agent mode stands while it works, as the chat shows it. */
sealed interface AgentStep {
    /** Move [number] of at most [AgentTaskUseCase.MAX_MOVES]: what it does ([move]) and on what ([target]). */
    data class Moving(
        val number: Int,
        val move: String,
        val target: String
    ) : AgentStep

    /** The changes it proposes, written out, waiting for the card's Do it, and what it found ([summary]). */
    data class Proposed(
        val steps: List<ActionStep>,
        val summary: String
    ) : AgentStep

    /** Nothing to change: what it found. */
    data class Finished(val summary: String) : AgentStep

    /** Its proposal could not be checked against the vault, or it ran out of moves or time. */
    data class Stopped(val problem: ActionProblem?) : AgentStep
}
