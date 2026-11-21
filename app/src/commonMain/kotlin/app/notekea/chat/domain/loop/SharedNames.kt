package app.notekea.chat.domain.loop

import app.notekea.chat.domain.loop.TaskFrame.Companion.bare
import app.notekea.chat.domain.tool.ToolCall

/**
 * A call that names a note by a bare name several notes share (Home/Budget 2026, Work/Budget 2026): what the model
 * is told, from what the task's frame says it means (`docs/agent-frame.md`): the note meant, each of them, or ask.
 */
internal object SharedNames {
    /** What the model is told when [call] names a note by a bare name several notes share; else null. */
    fun of(
        call: ToolCall,
        state: TurnState
    ): String? {
        val named = listOf(call.text(name = NOTE)) + call.list(name = NOTES)

        return named
            .filter { it.isNotEmpty() }
            .firstNotNullOfOrNull { name ->
                state
                    .turn
                    .sharing(name = name)
                    .takeIf { it.isNotEmpty() }
                    ?.let { paths ->
                        toldOf(
                            name = name,
                            paths = paths,
                            frame = state.frame
                        )
                    }
            }
    }

    private fun toldOf(
        name: String,
        paths: List<String>,
        frame: TaskFrame?
    ): String {
        val shared = "(${paths.size} notes are named \"$name\": ${paths.joinToString()}."

        val meant = frame
            ?.notes
            ?.map { it.bare() }
            ?.filter {
                it in paths
            }
            .orEmpty()

        return when {
            frame?.isSeveral == true -> "$shared The task means them all: call again with each whole path.)"

            meant.size == 1 -> "$shared The task means ${meant.single()}: call again with that whole path.)"

            meant.size > 1 ->
                "$shared The task means one of them and does not say which: ask_user with " +
                    "${meant.joinToString()} as the options.)"

            else ->
                "$shared Call again with the whole path of the one the task means, or with each when it " +
                    "means them all.)"
        }
    }

    private const val NOTE = "note"
    private const val NOTES = "notes"
}
