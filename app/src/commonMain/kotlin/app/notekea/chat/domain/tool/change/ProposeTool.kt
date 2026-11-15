package app.notekea.chat.domain.tool.change

import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolArgument
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.chat.domain.tool.ToolEffect
import app.notekea.chat.domain.tool.ToolResult

/**
 * The turn's end when it changes the vault: the changes added so far go on the card for the person's Do it, with a
 * line of what they are for. With no change added, the line is the turn's answer (`docs/agent-loop.md`, step 5).
 */
class ProposeTool : ChatTool {
    override val name = NAME

    override val description =
        "end the task: the changes you added are shown to the person to confirm, with one sentence saying " +
            "what they do. Call it once every change the task asks for is added. drop, optional: notes to take " +
            "out of the changes, the ones the task does not ask for."

    override val arguments = listOf(
        ToolArgument(name = SUMMARY),
        ToolArgument(
            name = DROP,
            isRequired = false,
            isList = true
        )
    )
    override val effect = ToolEffect.ENDS

    override fun targetOf(
        call: ToolCall,
        context: ToolContext
    ): String = ""

    override suspend fun run(
        call: ToolCall,
        context: ToolContext
    ): ToolResult = ToolResult(forModel = call.text(name = SUMMARY))

    companion object {
        const val NAME = "propose"
        const val SUMMARY = "summary"
        const val DROP = "drop"
    }
}
