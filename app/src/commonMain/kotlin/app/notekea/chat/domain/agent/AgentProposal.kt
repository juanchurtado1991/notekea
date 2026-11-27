package app.notekea.chat.domain.agent

import app.notekea.chat.domain.plan.PlannedStep

/**
 * How an agent's turn ends (4.7): the changes it proposes as [steps] of 3.9, or, when nothing is to change, its
 * [summary].
 */
data class AgentProposal(
    val summary: String? = null,
    val steps: List<PlannedStep> = emptyList()
)
