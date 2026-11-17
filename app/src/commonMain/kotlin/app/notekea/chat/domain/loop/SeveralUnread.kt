package app.notekea.chat.domain.loop

import app.notekea.chat.domain.tool.ToolCall
import app.notekea.chat.domain.tool.work.AnswerTool
import app.notekea.core.notes.domain.RelativePath

/**
 * An answer to a task about several notes, given with one of them unread: "which of my two budgets is larger?"
 * was answered "there is no second budget" from Home/Budget 2026 alone (the run after the bug hunt, the 4B).
 */
internal object SeveralUnread {
    /** What the model is told when [call] answers with a note of the same name as one it read still unread. */
    fun heldOf(
        call: ToolCall,
        state: TurnState
    ): String? {
        val isAboutOne = call.name != AnswerTool.NAME || AmbiguousTask.isSingular(task = state.task)
        if (isAboutOne) return null

        val read = state
            .turn
            .reads
            .read
            .toSet()

        val unread = read.firstNotNullOfOrNull { path ->
            state
                .turn
                .notes
                .map { it.file.path }
                .filter {
                    it != path && it.nameWithoutExtension == path.nameWithoutExtension
                }
                .filterNot {
                    it in read
                }
                .takeIf { it.isNotEmpty() }
        } ?: return null

        if (!state.told.add(element = TOLD)) return null

        val named = unread.joinToString {
            it.value.removeSuffix(suffix = RelativePath.MARKDOWN_EXTENSION)
        }

        return "(Not done: the task means them all, and you have not read $named, named as the one you read. " +
            "Read it, then answer from all of them.)"
    }

    private const val TOLD = "several unread"
}
