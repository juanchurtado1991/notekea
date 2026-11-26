package app.notekea.chat.presentation

import app.notekea.chat.domain.agent.AgentUse
import app.notekea.chat.presentation.model.ChatMode
import app.notekea.chat.presentation.model.ChatState
import app.notekea.chat.presentation.viewmodel.ChatReply
import kotlin.test.Test
import kotlin.test.assertEquals

/** What each mode does: Auto picks Deep thinking and the agent by the request, the others are what was chosen. */
class ChatModeTest {
    private val reasoning = ChatReply.Question(question = "compare Deep Work and Atomic Habits")
    private val oneFact = ChatReply.Question(question = "how many servings do the arepas make?")
    private val medium = ChatState(
        isDeepOffered = true,
        isAgentOffered = true,
        isDeepPicked = true,
        isAgentPicked = true
    )

    @Test
    fun `auto thinks deeply only about a question that asks to reason, where the model offers it and it was measured to help`() {
        val auto = medium.copy(mode = ChatMode.AUTO)

        assertEquals(
            expected = listOf(true, false, false, false, false, true, false),
            actual = listOf(
                auto.isDeepFor(reply = reasoning),
                auto.isDeepFor(reply = oneFact),
                auto.copy(isDeepOffered = false).isDeepFor(reply = reasoning),
                auto.copy(isDeepPicked = false).isDeepFor(reply = reasoning),
                auto.isDeepFor(reply = ChatReply.Overview),
                medium.copy(mode = ChatMode.DEEP).isDeepFor(reply = oneFact),
                medium.copy(mode = ChatMode.FAST).isDeepFor(reply = reasoning)
            )
        )
    }

    @Test
    fun `the agent works when chosen, when Auto or Deep thinking is stuck where it was measured to its target, never unoffered`() {
        assertEquals(
            expected = listOf(
                AgentUse.WHEN_STUCK,
                AgentUse.NEVER,
                AgentUse.WHEN_STUCK,
                AgentUse.FOR_WORK,
                AgentUse.NEVER,
                AgentUse.NEVER,
                AgentUse.NEVER,
                AgentUse.FOR_WORK
            ),
            actual = listOf(
                medium.copy(mode = ChatMode.AUTO).agentUse,
                medium.copy(mode = ChatMode.FAST).agentUse,
                medium.copy(mode = ChatMode.DEEP).agentUse,
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
                    mode = ChatMode.DEEP,
                    isAgentPicked = false
                ).agentUse,
                medium.copy(
                    mode = ChatMode.AGENT,
                    isAgentPicked = false
                ).agentUse
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
