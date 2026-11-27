package app.notekea.chat.presentation.model

import app.notekea.chat.domain.agent.AgentUse

/**
 * How the chat answers, chosen under its question field: [AUTO] picks for each request, the rest are what the person
 * chose. Agent mode (4.7) only where the model offers it.
 */
enum class ChatMode {
    /** Fast, and the agent, where it was measured to its target, for what the plan cannot do. */
    AUTO,

    /** One answer, the plan of 3.9: the quickest. */
    FAST,

    /** The agent for questions and actions; help, the overview and talk stay with the plan. */
    AGENT;

    /**
     * When a request of this mode goes to the agent: chosen, where the model offers it ([isAgentOffered]); picked
     * when stuck, only where it was measured to its target ([isAgentPicked]).
     */
    fun agentUse(
        isAgentOffered: Boolean,
        isAgentPicked: Boolean
    ): AgentUse = when {
        !isAgentOffered -> AgentUse.NEVER
        this == AGENT -> AgentUse.FOR_WORK
        this == AUTO && isAgentPicked -> AgentUse.WHEN_STUCK
        else -> AgentUse.NEVER
    }
}
