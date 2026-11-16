package app.notekea.chat.domain.loop

import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolEffect
import app.notekea.core.notes.domain.RelativePath

/**
 * A change made to a note the memory says the task does not mean (`docs/agent-memory.md`): "my goals" is Work/Goals
 * 2027 and the change names Personal/Goals 2027. Held back once with the note the memory means; made again, it stands.
 */
internal object MemoryChoice {
    fun heldOf(
        call: ToolCall,
        state: TurnState,
        tool: ChatTool?
    ): String? {
        val isChange = tool?.effect == ToolEffect.PROPOSES
        if (!isChange || state.settled.isEmpty()) return null

        val named = (listOf(call.text(name = NOTE)) + call.list(name = NOTES))
            .filter { it.isNotEmpty() }
            .mapNotNull { state.turn.pathOf(name = it) }

        val missed = state.settled.firstOrNull { settled ->
            val others = settled.others.map { bare(path = it) }
            named.any { it in others } && bare(path = settled.meant) !in named
        } ?: return null

        if (!state.told.add(element = TOLD)) return null

        val meant = bare(path = missed.meant)

        return "(Not added: the person's memory says the task's words mean $meant. Use that note, unless the task " +
            "itself says another.)"
    }

    private fun bare(path: RelativePath): String =
        path.value.removeSuffix(suffix = RelativePath.MARKDOWN_EXTENSION)

    private const val NOTE = "note"
    private const val NOTES = "notes"
    private const val TOLD = "memory choice"
}
