package app.notekea.chat.domain.agent

import app.notekea.chat.domain.plan.PlannedStep
import app.notekea.chat.domain.prompt.AgentTaskPrompt

/**
 * One move of agent mode (4.7), as the model chooses it: a look-up (search or find with a [query], read a [note], the
 * tasks, fetch a [url] the request wrote), or its end: propose the changes as [steps] of 3.9, or finish with a
 * [summary] when nothing is to change. The moves are named in [AgentTaskPrompt].
 */
data class AgentTaskMove(
    val move: String,
    val query: String? = null,
    val note: String? = null,
    val url: String? = null,
    val summary: String? = null,
    val steps: List<PlannedStep> = emptyList()
)
