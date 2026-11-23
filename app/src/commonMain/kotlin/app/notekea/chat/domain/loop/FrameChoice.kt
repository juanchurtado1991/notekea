package app.notekea.chat.domain.loop

import app.notekea.chat.domain.tool.ChatTool
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.ToolEffect
import app.notekea.chat.domain.tool.work.AnswerTool
import app.notekea.core.notes.domain.RelativePath

/**
 * A turn that goes against the one note its frame says the task means (`docs/agent-frame.md`): a change to another
 * note it was shown, or an answer from another one with the note meant unread, or from both. Held back once each,
 * with the note meant: the frame was settled by the task's words or the person's memory, which win over a guess.
 */
internal object FrameChoice {
    fun heldOf(
        call: ToolCall,
        state: TurnState,
        tool: ChatTool?
    ): String? {
        val frame = state.frame ?: return null
        val meant = frame.meant ?: return null

        return when {
            tool?.effect == ToolEffect.PROPOSES -> changedOf(call, state, frame, meant)
            call.name == AnswerTool.NAME -> answeredOf(state, frame, meant)
            else -> null
        }
    }

    private fun changedOf(
        call: ToolCall,
        state: TurnState,
        frame: TaskFrame,
        meant: RelativePath
    ): String? {
        val named = (listOf(call.text(name = NOTE)) + call.list(name = NOTES))
            .filter { it.isNotEmpty() }
            .mapNotNull { state.turn.noteNamed(name = it) }

        val isOther = named.any { it in frame.others } && meant !in named
        val isHeld = isOther && state.told.add(element = CHANGED)
        if (!isHeld) return null

        return "(Not added: the task means ${meant.bare()}. Use that note, unless the task itself says another.)"
    }

    // An answer from another note the task's words were shown with: "Tomás from the chess club" answered with the
    // phone of the first Tomás read, or the budget's total given with the other budget's figures.
    private fun answeredOf(
        state: TurnState,
        frame: TaskFrame,
        meant: RelativePath
    ): String? {
        val read = state
            .turn
            .reads
            .read
            .toSet()

        val others = frame.others.filter {
            it in read && it.nameWithoutExtension == meant.nameWithoutExtension
        }

        val isAnother = meant !in read && frame.others.any { it in read }

        return when {
            isAnother && state.told.add(element = UNREAD) ->
                "(Not done: the task means ${meant.bare()}, and you have not read it. Read it and answer from it.)"

            others.isNotEmpty() && state.told.add(element = ALONE) ->
                "(Not done: the task means ${meant.bare()}. Answer from it alone: leave " +
                    "${others.joinToString { it.bare() }} out of the answer, its figures included.)"

            else -> null
        }
    }

    private const val NOTE = "note"
    private const val NOTES = "notes"
    private const val CHANGED = "frame changed"
    private const val UNREAD = "frame unread"
    private const val ALONE = "frame alone"
}
