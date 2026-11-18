package app.notekea.chat.domain.loop

import app.notekea.chat.domain.loop.TaskFrame.Companion.bare
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.work.AnswerTool

/**
 * An answer to a task its frame says means several notes, given with some of them unread (`docs/agent-frame.md`):
 * "which of my two budgets is larger?" was answered "there is no second budget" from one budget alone. Held back
 * once, the model told which notes it has not read.
 */
internal object SeveralUnread {
    fun heldOf(
        call: ToolCall,
        state: TurnState
    ): String? {
        val frame = state.frame ?: return null
        val isSeveral = call.name == AnswerTool.NAME && frame.count == FrameCount.SEVERAL
        if (!isSeveral) return null

        val read = state
            .turn
            .reads
            .read
            .toSet()

        val unread = frame.notes.filterNot {
            it in read
        }

        val isHeld = unread.isNotEmpty() && state.told.add(element = TOLD)
        if (!isHeld) return null

        return "(Not done: the task means each of ${frame.notes.joinToString { it.bare() }}, and you have not " +
            "read ${unread.joinToString { it.bare() }}. Read it, then answer from all of them.)"
    }

    private const val TOLD = "several unread"
}
