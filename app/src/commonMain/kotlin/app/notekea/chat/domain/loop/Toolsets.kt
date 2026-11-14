package app.notekea.chat.domain.loop

import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolRegistry

/**
 * The tools a turn is given for its kind of work, and no others, [MOST_TOOLS] at most: a set missing the tool a task
 * needs costs more than the choice among more tools does (`docs/agent-loop.md`, steps 0 and 5). A kind with no set
 * works as a question does.
 */
class Toolsets(private val byKind: Map<WorkKind, List<ChatTool>>) {
    init {
        byKind.forEach { (kind, tools) ->
            require(tools.size <= MOST_TOOLS) {
                "A toolset holds $MOST_TOOLS tools at most: ${kind.written} has ${tools.size}"
            }
        }
    }

    fun of(kind: WorkKind): ToolRegistry =
        ToolRegistry(tools = byKind[kind] ?: byKind[WorkKind.ASK].orEmpty())

    companion object {
        const val MOST_TOOLS = 24
    }
}
