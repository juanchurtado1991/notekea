package app.notekea.chat.domain.tool.lookup

import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolArgument
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.chat.domain.tool.ToolEffect
import app.notekea.chat.domain.tool.ToolResult

/**
 * The parts of the turn's earlier results that are no longer shown, about what the model asks
 * (`docs/agent-step-memory.md`): it does not have to keep in mind what it read, it asks for it back.
 */
class RecallTool : ChatTool {
    override val name = NAME

    override val description =
        "what you need from earlier results that are no longer shown (\"memorizing tips\", \"the price of the " +
            "sensor\"); you get back their parts about it, without reading or searching again."

    override val arguments = listOf(ToolArgument(name = ABOUT))

    override val effect = ToolEffect.READS

    override fun targetOf(
        call: ToolCall,
        context: ToolContext
    ): String = call.text(name = ABOUT)

    // The loop answers it from the turn's step memory; without one, nothing was kept aside.
    override suspend fun run(
        call: ToolCall,
        context: ToolContext
    ): ToolResult = ToolResult(forModel = NOTHING)

    companion object {
        const val NAME = "recall"
        const val ABOUT = "about"
        const val NOTHING =
            "recall: nothing is kept aside; every result of this turn is still shown above."
    }
}
