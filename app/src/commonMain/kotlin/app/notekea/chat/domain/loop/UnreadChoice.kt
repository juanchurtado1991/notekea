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
        // Notes the frame says the task cannot tell apart are not told apart by reading them.
        val isChoice = call.name == AskUserTool.NAME && state.frame?.isOpen != true
        if (!isChoice) return null

        val options = AskUserTool.optionsOf(call = call)

        if (isOneName(options = options, state = state)) return null

        val read = state
            .turn
            .reads
            .read
            .toSet()

        val unread = options
            .mapNotNull {
                noteOf(
                    option = it,
                    state = state
                )
            }
            .filter {
                it !in read
            }
            .map { it.bare() }

        val isHeld = unread.isNotEmpty() && state.told.add(UNREAD)

        return if (isHeld) {
            "(Not asked yet: you have not read ${unread.joinToString()}. Read them to see which one the task " +
                "means, then do it; ask only if they still leave it open.)"
        } else {
            null
        }
    }

    // The paths of one bare name several notes share: the question `SharedNames` itself told the model to ask
    // ("2 notes are named Goals 2027 … ask_user with them as the options"), held back as unread (the bug hunt of
    // 2026-11-17, L4).
    private fun isOneName(
        options: List<String>,
        state: TurnState
    ): Boolean {
        val names = options
            .map { titleOf(option = it) }
            .distinct()

        val name = names.singleOrNull() ?: return false

        return options.size > 1 && state.turn.sharing(name = name).size > 1
    }

    private fun titleOf(option: String): String = option
        .trim()
        .removeSuffix(suffix = RelativePath.MARKDOWN_EXTENSION)
        .substringAfterLast(delimiter = '/')
        .lowercase()

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
                    .bare()
                    .lowercase()

                path == wanted || path.substringAfterLast(delimiter = '/') == wanted
            }
    }

    private const val UNREAD = "unread choice"
}
