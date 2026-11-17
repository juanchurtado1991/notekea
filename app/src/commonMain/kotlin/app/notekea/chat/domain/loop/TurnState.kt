package app.notekea.chat.domain.loop

import app.notekea.chat.domain.context.ContextBudget
import app.notekea.chat.domain.plan.ActionContext
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.chat.domain.tool.ToolRegistry
import app.notekea.core.memory.domain.MemoryLine

/**
 * One turn of the loop as it goes, held inside [RunTurnUseCase]'s flow and nowhere else: what its calls gave back
 * ([found]), the calls [made], the ones made again ([repeated]) and what the budget [dropped].
 */
internal class TurnState(
    val task: String,
    val context: ActionContext,
    val system: String,
    val tools: ToolRegistry,
    val turn: ToolContext,
    val budget: ContextBudget,
    val memory: List<MemoryLine> = emptyList(),
    val apart: List<AmbiguousTask.Settled> = emptyList()
) {
    val found = mutableListOf<String>()
    val made = mutableSetOf<String>()

    // The calls made again, and what the budget dropped at each fit: lists, as the domain holds no `var`.
    val repeated = mutableListOf<String>()
    val drops = mutableListOf<Int>()

    // The results not shown whole at the last step (dropped or folded), and where each call's result is in [found].
    val hidden = mutableSetOf<Int>()
    val foundAt = mutableMapOf<String, Int>()

    // The changes refused, so one refused again as it was counts as made again.
    val refused = mutableListOf<String>()

    // The tool of the last call made, when it was refused: not offered until another call is made.
    val withheld = mutableListOf<String>()

    /** The notes the task can mean and does not say which of: the person is asked before the turn ends. */
    val ambiguous by lazy {
        AmbiguousTask.notesOf(
            task = task,
            notes = turn.notes,
            settled = memory.flatMap { it.names },
            told = apart
        )
    }

    /** The sets of notes the memory or the notes settled for the task: another of them picked is held back once. */
    val settled by lazy {
        TurnSetup.settledOf(
            task = task,
            notes = turn.notes,
            remembered = memory,
            told = apart
        )
    }

    val dropped: Int get() = drops.lastOrNull() ?: 0

    // What the turn was told once, so it is not told again: that it proposed with nothing added.
    val told = mutableSetOf<String>()

    /** A call made again three times is a loop: the next call may only end the turn, a grammar seeing to it. */
    val mustEnd: Boolean get() = repeated.size >= MOST_REPEATS

    /**
     * Twice that many: the turn may only end, no change tool offered. Two refused changes took turns for 28 steps
     * (the loop's eighth run), as a change tool is still an ending call.
     */
    val mustStop: Boolean get() = repeated.size >= MOST_REPEATS * 2

    private companion object {
        const val MOST_REPEATS = 3
    }
}
