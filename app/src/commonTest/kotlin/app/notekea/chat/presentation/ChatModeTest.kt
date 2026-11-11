package app.notekea.chat.presentation

import app.notekea.chat.domain.AgentUse
import kotlin.test.Test
import kotlin.test.assertEquals

/** What each mode does: Auto picks Deep thinking and the agent by the request, the others are what was chosen. */
class ChatModeTest {
    private val reasoning = ChatReply.Question(question = "compare Deep Work and Atomic Habits")
    private val oneFact = ChatReply.Question(question = "how many servings do the arepas make?")
    private val medium = ChatState(
        isDeepOffered = true,
        isAgentOffered = true
    )

    @Test
    fun `auto thinks deeply only about a question that asks to reason, and only where the model offers it`() {
        val auto = medium.copy(mode = ChatMode.AUTO)

        assertEquals(
            expected = listOf(true, false, false, false, true, false),
            actual = listOf(
                auto.isDeepFor(reply = reasoning),
                auto.isDeepFor(reply = oneFact),
                auto.copy(isDeepOffered = false).isDeepFor(reply = reasoning),
                auto.isDeepFor(reply = ChatReply.Overview),
                medium.copy(mode = ChatMode.DEEP).isDeepFor(reply = oneFact),
                medium.copy(mode = ChatMode.FAST).isDeepFor(reply = reasoning)
            )
        )
    }

    @Test
    fun `the agent works when chosen, when Auto's plan is stuck, and never where it is not offered`() {
        assertEquals(
            expected = listOf(
                AgentUse.WHEN_STUCK,
                AgentUse.NEVER,
                AgentUse.NEVER,
                AgentUse.FOR_WORK,
                AgentUse.NEVER
            ),
            actual = listOf(
                ChatMode.AUTO.agentUse(isAgentOffered = true),
                ChatMode.FAST.agentUse(isAgentOffered = true),
                ChatMode.DEEP.agentUse(isAgentOffered = true),
                ChatMode.AGENT.agentUse(isAgentOffered = true),
                ChatMode.AGENT.agentUse(isAgentOffered = false)
            )
        )
    }

    @Test
    fun `a model offers Auto and Fast always, Deep thinking and the agent where they help`() {
        assertEquals(
            expected = listOf(ChatMode.entries.toList(), listOf(ChatMode.AUTO, ChatMode.FAST)),
            actual = listOf(medium.modes, ChatState().modes)
        )
    }
}
