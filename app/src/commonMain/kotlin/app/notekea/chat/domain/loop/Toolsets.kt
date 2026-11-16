package app.notekea.chat.domain.loop

import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolEffect
import app.notekea.chat.domain.tool.ToolRegistry
import app.notekea.chat.domain.tool.change.ChangeTools
import app.notekea.chat.domain.tool.lookup.FetchTool
import app.notekea.chat.domain.tool.web.WebSearchTool

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

    /**
     * The tools for [kind], and for a note written from the web ([WebAsked]) the web's tools to search, read and clip
     * as well: the router sends "research it and write a note" to the writing work, which has none of them.
     */
    fun of(
        kind: WorkKind,
        task: String
    ): ToolRegistry {
        val tools = byKind[kind] ?: byKind[WorkKind.ASK].orEmpty()
        val isWritten = kind == WorkKind.WRITE && WebAsked.isIn(task = task)
        if (!isWritten) return ToolRegistry(tools = tools)

        val names = tools.map { it.name }.toSet()

        val web = byKind[WorkKind.WEB]
            .orEmpty()
            .filter {
                it.name in WEB_TOOLS && it.name !in names
            }

        // Before the ends, so the model meets them among the tools that look.
        val (ends, looks) = tools.partition {
            it.effect == ToolEffect.ENDS
        }

        return ToolRegistry(tools = (looks + web + ends).take(n = MOST_TOOLS))
    }

    companion object {
        const val MOST_TOOLS = 24

        private val WEB_TOOLS = setOf(WebSearchTool.NAME, FetchTool.NAME, ChangeTools.clip.name)
    }
}
