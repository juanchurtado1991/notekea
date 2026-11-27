package app.notekea.chat.presentation

import app.notekea.chat.domain.agent.AgentUse
import app.notekea.chat.presentation.model.ChatMode
import app.notekea.chat.presentation.model.ChatState
import kotlin.test.Test
import kotlin.test.assertEquals

/** What each mode does: Auto hands the agent what the plan cannot do, the others are what was chosen. */
class ChatModeTest {
    private val medium = ChatState(
        isAgentOffered = true,
        isAgentPicked = true
    )

    @Test
    fun `the agent works when chosen, when Auto is stuck where it was measured to its target, never unoffered`() {
        assertEquals(
            expected = listOf(
                AgentUse.WHEN_STUCK,
                AgentUse.NEVER,
                AgentUse.FOR_WORK,
                AgentUse.NEVER,
                AgentUse.NEVER,
                AgentUse.FOR_WORK
            ),
            actual = listOf(
                medium.copy(mode = ChatMode.AUTO).agentUse,
                medium.copy(mode = ChatMode.FAST).agentUse,
                medium.copy(mode = ChatMode.AGENT).agentUse,
                medium.copy(
                    mode = ChatMode.AGENT,
                    isAgentOffered = false
                ).agentUse,
                medium.copy(
                    mode = ChatMode.AUTO,
                    isAgentPicked = false
                ).agentUse,
                medium.copy(
                    mode = ChatMode.AGENT,
                    isAgentPicked = false
                ).agentUse
            )
        )
    }

    // The developer, 2026-11-26: Deep thinking taken out, never better than one draft and three times as slow.
    @Test
    fun `a model offers Auto and Fast always, and the agent where it helps`() {
        assertEquals(
            expected = listOf(ChatMode.entries.toList(), listOf(ChatMode.AUTO, ChatMode.FAST)),
            actual = listOf(medium.modes, ChatState().modes)
        )
    }
}
