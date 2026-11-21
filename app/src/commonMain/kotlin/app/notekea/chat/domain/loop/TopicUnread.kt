package app.notekea.chat.domain.loop

import app.notekea.chat.domain.loop.TaskFrame.Companion.bare
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.lookup.ReadTool
import app.notekea.chat.domain.tool.work.AnswerTool

/**
 * An answer given while notes about the same topic as the ones read, that write the task's words, are still unread
 * (`docs/agent-figures.md`): "papá is not allergic to anything", with his penicillin allergy in the care plan. Held
 * back once, the model told to read them first.
 */
internal object TopicUnread {
    fun heldOf(
        call: ToolCall,
        state: TurnState
    ): String? {
        if (call.name != AnswerTool.NAME) return null

        val related = state.turn.reads.related
        val isTold = related.isEmpty() || !state.told.add(element = TOLD)
        if (isTold) return null

        // The next call may only read: told to, the 4B ran out of steps in 13 of its 14 failures with this hold
        // (`holds.py`, the dev run of 2026-11-20).
        state.narrowed += ReadTool.NAME

        return "(Not done: ${related.joinToString { it.bare() }} ${if (related.size == 1) "is" else "are"} about " +
            "the same topic as what you read and write the task's words, and you have not read " +
            "${if (related.size == 1) "it" else "them"}. Read before you answer, above all before saying the notes " +
            "do not hold something.)"
    }

    private const val TOLD = "topic unread"
}
