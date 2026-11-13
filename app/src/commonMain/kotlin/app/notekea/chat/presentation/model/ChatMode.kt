package app.notekea.chat.presentation.model

import app.notekea.chat.domain.agent.AgentUse

/**
 * How the chat answers, chosen under its question field: [AUTO] picks for each request, the rest are what the person
 * chose. Deep thinking (3.6) and agent mode (4.7) only where the model offers them.
 */
enum class ChatMode {
    /** Fast, Deep thinking for a question that asks to reason, the agent for an action the plan cannot name. */
    AUTO,

    /** One answer, the plan of 3.9: the quickest. */
    FAST,

    /** Three drafts and a vote for questions: slower, more often right. */
    DEEP,

    /** The agent for questions and actions; help, the overview and talk stay with the plan. */
    AGENT;

    /** When a request of this mode goes to the agent, the model offering it as [isAgentOffered] says. */
    fun agentUse(isAgentOffered: Boolean): AgentUse = when {
        !isAgentOffered -> AgentUse.NEVER

        this == AGENT -> AgentUse.FOR_WORK

        // Deep thinking looks further too: "what did I learn from my books?" was left unanswered (the probe).
        this == AUTO || this == DEEP -> AgentUse.WHEN_STUCK

        else -> AgentUse.NEVER
    }
}
