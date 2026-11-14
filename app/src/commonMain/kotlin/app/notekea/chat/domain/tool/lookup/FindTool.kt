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

/** The notes chosen by the values of their properties, asked for in plain words (3.8). */
class FindTool(actionRunner: ActionRunner) : ChatTool {
    private val foundRows = FoundRows(actionRunner = actionRunner)

    override val name = NAME

    override val description =
        "notes chosen by their properties, in plain words, naming the property the task means as the vault " +
            "lists it (\"books rated 4 or more\", \"books with finished true\"); never another property in " +
            "its place."

    override val arguments = listOf(ToolArgument(name = QUERY))
    override val effect = ToolEffect.READS

    override fun targetOf(
        call: ToolCall,
        context: ToolContext
    ): String = call.text(name = QUERY)

    override suspend fun run(
        call: ToolCall,
        context: ToolContext
    ): ToolResult = foundRows.of(
        step = ActionStep(
            kind = ActionKind.FIND_NOTES,
            request = call.arguments[QUERY]
        ),
        context = context
    )

    companion object {
        const val NAME = "find"
        const val QUERY = "query"
    }
}
