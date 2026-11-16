package app.notekea.chat.domain.loop

import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.work.AskUserTool

/**
 * A question whose answers all name the one same note ("2026-10-21 Kickoff" or "Meetings/2026-10-21 Kickoff"): there
 * is nothing to choose, so it is held back once and the model told which note they both are.
 */
internal object SameNoteQuestion {
    fun heldOf(
        call: ToolCall,
        state: TurnState
    ): String? {
        if (call.name != AskUserTool.NAME) return null

        val options = AskUserTool.optionsOf(call = call)
        if (options.size < 2) return null

        val notes = options.map { state.turn.noteNamed(name = it) }
        val isOneNote = notes.all { it != null } && notes.distinct().size == 1
        if (!isOneNote) return null

        val path = notes
            .first()
            ?.value
            .orEmpty()

        val held =
            "(Not asked: ${options.joinToString(separator = " and ")} are the same note, $path, so " +
                "there is nothing to choose. Go on with it.)"

        return held.takeIf { state.told.add(element = it) }
    }
}
