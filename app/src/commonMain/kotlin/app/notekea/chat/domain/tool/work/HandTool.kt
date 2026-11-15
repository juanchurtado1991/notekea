package app.notekea.chat.domain.tool.work

import app.notekea.chat.domain.loop.WorkKind
import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolArgument
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.chat.domain.tool.ToolEffect
import app.notekea.chat.domain.tool.ToolResult

/**
 * A request that is not work for tools, handed to the chat's own reply of its [kind] (`docs/agent-loop.md`, step 6):
 * how to use the app, from its guide; what the vault holds, from its map. It ends the turn.
 */
class HandTool(
    val kind: WorkKind,
    override val description: String
) : ChatTool {
    override val name = kind.written

    override val arguments = emptyList<ToolArgument>()

    override val effect = ToolEffect.ENDS

    override fun targetOf(
        call: ToolCall,
        context: ToolContext
    ): String = ""

    override suspend fun run(
        call: ToolCall,
        context: ToolContext
    ): ToolResult = ToolResult(forModel = "$name: handed to the chat.")

    companion object {
        val help = HandTool(
            kind = WorkKind.HELP,
            description = "when the request asks how to do something in Notekea, or what the chat can do: the " +
                "app's guide answers it. Never do it for them."
        )

        val overview = HandTool(
            kind = WorkKind.OVERVIEW,
            description = "when the request asks what the vault holds as a whole: its map answers it."
        )
    }
}
