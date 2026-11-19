package app.notekea.chat.domain.loop

import app.notekea.chat.domain.loop.TaskFrame.Companion.bare
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.work.AnswerTool

/**
 * An answer given while notes about the same topic as the ones read, that write the task's words, are still unread
 * (`docs/agent-figures.md`): "papá is not allergic to anything", with his penicillin allergy in the care plan. Held
 * back once, and until they are read the calls may only read: told, the 4B ran out of steps instead.
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

        return "(Not done: ${related.joinToString { it.bare() }} ${if (related.size == 1) "is" else "are"} about " +
            "the same topic as what you read and write the task's words, and you have not read " +
            "${if (related.size == 1) "it" else "them"}. Read before you answer, above all before saying the notes " +
            "do not hold something.)"
    }

    /** Whether the next call may only read: the answer was held back for notes still unread. */
    fun mustRead(state: TurnState): Boolean {
        val isUnread = state
            .turn
            .reads
            .related
            .isNotEmpty()

        return TOLD in state.told && isUnread
    }

    private const val TOLD = "topic unread"
}
