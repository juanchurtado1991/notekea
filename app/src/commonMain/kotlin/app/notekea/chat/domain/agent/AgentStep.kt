package app.notekea.chat.domain.agent

import app.notekea.chat.domain.loop.WorkKind
import app.notekea.chat.domain.plan.ActionProblem
import app.notekea.chat.domain.plan.ActionStep
import app.notekea.core.web.domain.FoundSource

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

    /** A question to the person in place of a guess, with the [options] to choose from as buttons. */
    data class Asked(
        val question: String,
        val options: List<String>,
        val remembering: Remembering? = null
    ) : AgentStep

    /**
     * A web search found sources the person has not trusted yet (`docs/agent-web.md`): the turn stops so they approve
     * the ones to read, the trusted ones ticked already.
     */
    data class SourcesFound(
        val sources: List<FoundSource>,
        val omitted: Int
    ) : AgentStep

    /** Steps the app shows now (a note opened, a panel, the theme): nothing in the notes changes. */
    data class Shown(val steps: List<ActionStep>) : AgentStep

    /** Not work for tools: the chat's own reply of this [kind] (talk, help, the vault's overview) answers it. */
    data class Replied(val kind: WorkKind) : AgentStep

    /** Its proposal could not be checked against the vault, or it ran out of moves or time. */
    data class Stopped(val problem: ActionProblem?) : AgentStep
}
