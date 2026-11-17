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
                        toldOf(
                            name = name,
                            paths = paths,
                            task = state.task
                        )
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

    private fun toldOf(
        name: String,
        paths: List<String>,
        task: String
    ): String {
        val shared = "(${paths.size} notes are named \"$name\": ${paths.joinToString()}."

        // "the work budget" with Home/Budget 2026 and Work/Budget 2026 says which: its folder is a word of the task
        // (the bug hunt of 2026-11-17, L5, where the model was told to ask).
        val said = wordsOf(text = task).toSet()

        val inFolder = paths.singleOrNull { path ->
            val title = wordsOf(text = path.substringAfterLast(delimiter = '/'))

            folderWordsOf(path = path).any {
                it in said && it !in title
            }
        }

        if (inFolder != null) {
            return "$shared The task names the folder of $inFolder, so it means that one: call again with " +
                "that whole path.)"
        }

        // "both of my Goals 2027 notes", "my two budgets" mean them all: told to ask, the models asked (the run
        // after the bug hunt), so a task about several is told only to read each.
        if (!AmbiguousTask.isSingular(task = task)) {
            return "$shared The task means them all: call again with each whole path.)"
        }

        return "$shared When the task means one of them and does not say which, ask_user with them as the " +
            "options. When it means them all, or says which, call again with each whole path.)"
    }

    private fun folderWordsOf(path: String): List<String> = wordsOf(
        text = path.substringBeforeLast(
            delimiter = '/',
            missingDelimiterValue = ""
        )
    ).filter {
        it.length >= MIN_FOLDER_WORD
    }

    private fun wordsOf(text: String): List<String> = text
        .lowercase()
        .split(NOT_WORD)
        .filter { it.isNotEmpty() }

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

    private const val MIN_FOLDER_WORD = 3
    private val NOT_WORD = Regex(pattern = "[^\\p{L}\\p{N}]+")

    private const val NOTE = "note"
    private const val NOTES = "notes"
}
