package app.notekea.chat.domain.loop

import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolEffect
import app.notekea.chat.domain.tool.app.ShowTool
import app.notekea.chat.domain.tool.work.AskUserTool

/**
 * An end made where the task's frame is open (`docs/agent-frame.md`): the task means one note, or one entry of a
 * note, and its words fit several. Held back once, and the next call may only ask the person which; an end that
 * covers them all is a guess too, as the task means one ("Laura's phone" answered with both Lauras' phones).
 */
internal object UnsettledEnd {
    /** What the model is told when [call] ends without asking what the frame left open, else null. */
    fun heldOf(
        call: ToolCall,
        state: TurnState,
        tool: ChatTool?
    ): String? {
        val isEnd = tool is ShowTool || tool?.effect == ToolEffect.ENDS
        if (!isEnd || call.name == AskUserTool.NAME) return null

        val frame = state.frame?.takeIf { it.isOpen } ?: return null
        if (!state.told.add(MUST_ASK)) return null

        val what = if (frame.openNotes.isNotEmpty()) "notes" else "entries"

        return "(Not done: the task means one of ${frame.options.size} $what and does not say which: " +
            "${frame.options.joinToString(separator = "; ")}. Ask the person with ask_user, these as the options.)"
    }

    /** Whether the next call may only be ask_user: the turn was held back for a question it must ask. */
    fun mustAsk(state: TurnState): Boolean = MUST_ASK in state.told

    const val MUST_ASK = "must ask"
}
