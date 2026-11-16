package app.notekea.chat.domain.loop

import app.notekea.chat.domain.context.StepMemory
import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolRegistry
import app.notekea.chat.domain.tool.lookup.RecallTool

/**
 * The loop's side of the memory of the turn's steps (`docs/agent-step-memory.md`): with one, the model is given
 * [RecallTool] and asks back what it read instead of keeping it in mind.
 */
internal object StepRecall {
    fun toolsOf(
        tools: ToolRegistry,
        memory: StepMemory?
    ): ToolRegistry =
        if (memory == null) tools else ToolRegistry(tools = tools.tools + RecallTool())

    /** What [call] gave back; a recall is answered from what the turn kept aside. */
    suspend fun resultOf(
        call: ToolCall,
        tool: ChatTool,
        state: TurnState
    ): String {
        if (tool !is RecallTool) return tool.run(call, state.turn).forModel

        return state
            .budget
            .recalled(
                about = call.text(name = RecallTool.ABOUT),
                entries = state.found,
                dropped = state.dropped
            )
            ?: RecallTool.NOTHING
    }
}
