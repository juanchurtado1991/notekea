package app.notekea.chat.domain.loop

import app.notekea.chat.domain.agent.Remembering
import app.notekea.chat.domain.tool.ToolCall
import app.notekea.core.notes.domain.RelativePath

/**
 * Names several notes share, as a turn of the loop meets them: told to the model to ask or to say which, unless the
 * memory settles them; and, once the person is asked, what their answer can be remembered as (`docs/agent-memory.md`).
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
                    .takeIf { paths ->
                        paths.isNotEmpty() && paths.none {
                            isRemembered(
                                path = it,
                                state = state
                            )
                        }
                    }
                    ?.let { paths ->
                        "(${paths.size} notes are named \"$name\": ${paths.joinToString()}. When the task " +
                            "means one of them and does not say which, ask_user with them as the options. " +
                            "When it means them all, or says which, call again with each whole path.)"
                    }
            }
    }

    /** What a question about the notes the task left open can be remembered as: the words that meant them. */
    fun rememberingOf(state: TurnState): Remembering? {
        val open = state.ambiguous.takeIf { it.isNotEmpty() } ?: return null

        return AmbiguousTask
            .saidOf(
                task = state.task,
                paths = open
            )
            ?.let {
                Remembering(
                    said = it,
                    notes = open
                )
            }
    }

    // One of the notes is the one the memory means: no question.
    private fun isRemembered(
        path: String,
        state: TurnState
    ): Boolean = state
        .turn
        .remembered
        .any {
            val remembered = it.value.removeSuffix(suffix = RelativePath.MARKDOWN_EXTENSION)
            remembered == path.removeSuffix(suffix = RelativePath.MARKDOWN_EXTENSION)
        }

    private const val NOTE = "note"
    private const val NOTES = "notes"
}
