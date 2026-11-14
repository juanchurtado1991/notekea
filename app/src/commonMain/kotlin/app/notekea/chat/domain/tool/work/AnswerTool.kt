package app.notekea.chat.domain.tool.work

import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolArgument
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.chat.domain.tool.ToolEffect
import app.notekea.chat.domain.tool.ToolResult

/** The turn's end when nothing is to change: what it found, said to the person (`docs/agent-loop.md`, step 5). */
class AnswerTool : ChatTool {
    override val name = NAME

    override val description =
        "end the task: your answer to the person, only from what the tools gave you, in the task's language."

    override val arguments = listOf(ToolArgument(name = TEXT))
    override val effect = ToolEffect.ENDS

    override fun targetOf(
        call: ToolCall,
        context: ToolContext
    ): String = ""

    override suspend fun run(
        call: ToolCall,
        context: ToolContext
    ): ToolResult = ToolResult(forModel = call.text(name = TEXT))

    companion object {
        const val NAME = "answer"
        const val TEXT = "text"
    }
}
