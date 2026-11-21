package app.notekea.chat.domain.loop

import app.notekea.chat.domain.agent.AgentFinalAnswer
import app.notekea.chat.domain.context.FittedContext
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
                hidden = state.hidden
            )
            ?: RecallTool.NOTHING
    }

    /**
     * What a read made again is told when its result is kept aside: the result again, whole, as "its result is
     * above" would not be true; null when it is still shown.
     */
    fun againOf(
        key: String,
        state: TurnState
    ): String? {
        val at = state.foundAt[key]?.takeIf {
            it in state.hidden
        } ?: return null

        return AgentFinalAnswer.SHOWN_AGAIN + state.found[at]
    }

    /** The prompt's results for the next step, the results it leaves hidden kept in [state]. */
    suspend fun shownOf(state: TurnState): FittedContext {
        val fitted = state.budget.shown(state.found, state.dropped)

        state.drops += fitted.dropped
        state.hidden.clear()
        state.hidden += fitted.hidden

        return fitted
    }

    /** The results shown whole when the model picks its next step, the longer ones before them folded. */
    const val WHOLE_LAST = 3
}
