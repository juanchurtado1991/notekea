package app.notekea.chat.domain.tool.lookup

import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.ActionRunner
import app.notekea.chat.domain.plan.ActionStep
import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolArgument
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.chat.domain.tool.ToolEffect
import app.notekea.chat.domain.tool.ToolResult

/** The vault's open tasks with their dates. */
class TasksTool(actionRunner: ActionRunner) : ChatTool {
    private val foundRows = FoundRows(actionRunner = actionRunner)

    override val name = NAME

    override val description =
        "the open tasks of the vault, with their dates: look here for what is late, overdue, due or next."

    override val arguments = emptyList<ToolArgument>()
    override val effect = ToolEffect.READS

    override fun targetOf(
        call: ToolCall,
        context: ToolContext
    ): String = ""

    override suspend fun run(
        call: ToolCall,
        context: ToolContext
    ): ToolResult = foundRows.of(
        step = ActionStep(kind = ActionKind.SHOW_TASKS),
        context = context
    )

    companion object {
        const val NAME = "tasks"
    }
}
