package app.notekea.chat.domain.loop

import app.notekea.chat.domain.context.ContextBudget
import app.notekea.chat.domain.plan.ActionContext
import app.notekea.chat.domain.tool.ToolContext
import app.notekea.chat.domain.tool.ToolRegistry

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
    val budget: ContextBudget
) {
    val found = mutableListOf<String>()
    val made = mutableSetOf<String>()

    // The calls made again, and what the budget dropped at each fit: lists, as the domain holds no `var`.
    val repeated = mutableListOf<String>()
    val drops = mutableListOf<Int>()

    val dropped: Int get() = drops.lastOrNull() ?: 0

    // What the turn was told once, so it is not told again: that it proposed with nothing added.
    val told = mutableSetOf<String>()

    /** A call made again three times is a loop: the next call may only end the turn, a grammar seeing to it. */
    val mustEnd: Boolean get() = repeated.size >= MOST_REPEATS

    private companion object {
        const val MOST_REPEATS = 3
    }
}
