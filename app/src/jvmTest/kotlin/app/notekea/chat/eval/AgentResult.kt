package app.notekea.chat.eval

import app.notekea.chat.domain.agent.AgentStep

/** One task of an evaluation set as it ran: its steps, how long it took, and what was wrong (null: right). */
internal class AgentResult(
    val case: AgentEvalCase,
    val steps: List<AgentStep>,
    val seconds: Long,
    val problem: String?
)
