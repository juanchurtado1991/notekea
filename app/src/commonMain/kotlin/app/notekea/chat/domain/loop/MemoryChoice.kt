package app.notekea.chat.domain.loop

import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolEffect
import app.notekea.chat.domain.tool.work.AnswerTool
import app.notekea.chat.domain.tool.work.AskUserTool
import app.notekea.core.notes.domain.RelativePath

/**
 * A turn that goes against a note settled for its task, by the memory (`docs/agent-memory.md`: "my goals" is
 * Work/Goals 2027) or by the task's own words (`ToldApart`): a change to another note of the set, a question asking
 * which, or an answer from another note with the one meant unread. Held back with the note meant.
 */
internal object MemoryChoice {
    fun heldOf(
        call: ToolCall,
        state: TurnState,
        tool: ChatTool?
    ): String? = when {
        state.settled.isEmpty() -> null

        tool?.effect == ToolEffect.PROPOSES -> changedOf(
            call = call,
            state = state
        )

        call.name == AnswerTool.NAME -> answeredOf(state = state)

        else -> null
    }

    /**
     * What the model is told when [call] asks the person which note of a settled set: twice at most, as the 4B asked
     * again right after it was told (the loop's fifth run), and then the question is asked.
     */
    fun askedOf(
        call: ToolCall,
        state: TurnState
    ): String? {
        if (call.name != AskUserTool.NAME) return null

        val options = AskUserTool
            .optionsOf(call = call)
            .map {
                it
                    .trim()
                    .removeSuffix(suffix = RelativePath.MARKDOWN_EXTENSION)
                    .lowercase()
            }

        val asked = state.settled.firstOrNull { settled ->
            (settled.others + settled.meant).count { path ->
                val whole = bare(path = path).lowercase()

                // An option names a note with words of its own too: "Goals 2027 (work)" is Work/Goals 2027.
                options.any {
                    it == whole || whole.substringAfterLast(delimiter = '/') in it
                }
            } >= 2
        } ?: return null

        val isHeld = state.told.add(element = ASKED) || state.told.add(element = ASKED_AGAIN)
        if (!isHeld) return null

        return "(Not asked: ${whyOf(settled = asked)}, so there is nothing to ask. Do the task with " +
            "${bare(path = asked.meant)} now.)"
    }

    private fun changedOf(
        call: ToolCall,
        state: TurnState
    ): String? {
        val named = (listOf(call.text(name = NOTE)) + call.list(name = NOTES))
            .filter { it.isNotEmpty() }
            .mapNotNull { state.turn.pathOf(name = it) }

        val missed = state.settled.firstOrNull { settled ->
            val others = settled.others.map { bare(path = it) }
            named.any { it in others } && bare(path = settled.meant) !in named
        } ?: return null

        if (!state.told.add(element = TOLD)) return null

        return "(Not added: ${whyOf(settled = missed)}. Use that note, unless the task itself says another.)"
    }

    // An answer from another note of the set, the one meant never read: "Tomás from the chess club" answered with
    // the phone of the first Tomás read.
    private fun answeredOf(state: TurnState): String? {
        val read = state
            .turn
            .reads
            .read
            .toSet()

        val missed = state.settled.firstOrNull { settled ->
            settled.meant !in read && settled.others.any { it in read }
        }

        if (missed != null) {
            if (!state.told.add(element = ANSWERED)) return null

            return "(Not done: ${whyOf(settled = missed)}, and you have not read it. Read " +
                "${bare(path = missed.meant)} and answer from it.)"
        }

        // Both read, the answer gave both: "77,600, the household's and the cooperative's" for "the budget", which
        // the memory says is the cooperative's (the run after the bug hunt, the 9B). Told once to keep to it.
        val both = state.settled.firstOrNull { settled ->
            settled.meant in read && settled.others.any { it in read }
        } ?: return null

        if (!state.told.add(element = ANSWERED_ALONE)) return null

        val others = both.others.joinToString { bare(path = it) }

        return "(Not done: ${whyOf(settled = both)}. Answer from ${bare(path = both.meant)} alone: leave $others " +
            "out of the answer, its figures included.)"
    }

    private fun whyOf(settled: AmbiguousTask.Settled): String {
        val meant = bare(path = settled.meant)

        return settled
            .said
            ?.let { "the task says \"$it\", and of the notes it could mean only $meant writes it" }
            ?: "the person's memory says the task's words mean $meant"
    }

    private fun bare(path: RelativePath): String =
        path.value.removeSuffix(suffix = RelativePath.MARKDOWN_EXTENSION)

    private const val NOTE = "note"
    private const val NOTES = "notes"
    private const val TOLD = "memory choice"
    private const val ASKED = "memory asked"
    private const val ASKED_AGAIN = "memory asked again"
    private const val ANSWERED = "memory answered"
    private const val ANSWERED_ALONE = "memory answered alone"
}
