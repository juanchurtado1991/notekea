package app.notekea.chat.domain.loop

import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.work.AskUserTool
import app.notekea.core.notes.domain.RelativePath

/**
 * A question whose answers are notes the turn has not read: which meeting is about the sidebar is in the meetings,
 * not for the person to say. Held back once, the model told to read them; asked again, it is asked.
 */
internal object UnreadChoice {
    /** What the model is told when [call] asks the person to choose among notes [state] has not read, else null. */
    fun of(
        call: ToolCall,
        state: TurnState
    ): String? {
        if (call.name != AskUserTool.NAME) return null

        val read = state
            .turn
            .reads
            .read
            .toSet()

        val unread = AskUserTool
            .optionsOf(call = call)
            .mapNotNull {
                noteOf(
                    option = it,
                    state = state
                )
            }
            .filter {
                it !in read
            }
            .map { it.value.removeSuffix(suffix = RelativePath.MARKDOWN_EXTENSION) }

        val isHeld = unread.isNotEmpty() && state.told.add(UNREAD)

        return if (isHeld) {
            "(Not asked yet: you have not read ${unread.joinToString()}. Read them to see which one the task " +
                "means, then do it; ask only if they still leave it open.)"
        } else {
            null
        }
    }

    // The note an answer names, by its path or its name, whatever its case.
    private fun noteOf(
        option: String,
        state: TurnState
    ): RelativePath? {
        val wanted = option
            .trim()
            .removeSuffix(suffix = RelativePath.MARKDOWN_EXTENSION)
            .lowercase()

        return state
            .turn
            .notes
            .map { it.file.path }
            .firstOrNull {
                val path = it
                    .value
                    .removeSuffix(suffix = RelativePath.MARKDOWN_EXTENSION)
                    .lowercase()

                path == wanted || path.substringAfterLast(delimiter = '/') == wanted
            }
    }

    private const val UNREAD = "unread choice"
}
