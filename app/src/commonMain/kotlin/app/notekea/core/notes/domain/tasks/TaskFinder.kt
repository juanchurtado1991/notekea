package app.notekea.core.notes.domain.tasks

import app.notekea.core.domain.StringPatterns.NEW_LINE_CHAR
import app.notekea.core.domain.forEachLineOutsideCode
import app.notekea.core.domain.lineEndAt
import app.notekea.core.domain.lineStartAt
import app.notekea.core.domain.markdown.FenceTracker
import app.notekea.core.domain.markdown.MarkdownSyntax

/**
 * The tasks of a note's text, as Obsidian reads them: a list item (`-`, `*`, `+` or `1.`), also inside quotes and
 * callouts, that starts with `[ ]`, `[x]` or `[X]` followed by a space or the end of the line. Lines in fenced code
 * blocks are not tasks.
 */
class TaskFinder {
    fun tasks(text: String): List<TaskMarker> {
        val tasks = mutableListOf<TaskMarker>()

        text.forEachLineOutsideCode { start, end ->
            markerIn(
                line = text.substring(
                    startIndex = start,
                    endIndex = end
                ),
                lineStart = start
            )?.let { tasks += it }
        }

        return tasks
    }

    /** The task of a [line] outside fenced code that starts at [lineStart] in its text, or null when it holds none. */
    fun markerIn(
        line: String,
        lineStart: Int
    ): TaskMarker? = TASK
        .find(input = line)
        ?.groups
        ?.get(index = 1)
        ?.let { state ->
            TaskMarker(
                stateIndex = lineStart + state.range.first,
                isDone = state.value.single() != MarkdownSyntax.TASK_OPEN_STATE
            )
        }

    /** The task on the line that holds [offset] (the cursor), or null when that line is not a task. */
    fun taskAt(
        text: String,
        offset: Int
    ): TaskMarker? {
        val lineStart = text.lineStartAt(index = offset)

        val lineEnd = text.lineEndAt(index = offset)

        return tasks(text = text).firstOrNull {
            it.stateIndex in lineStart until lineEnd
        }
    }

    companion object {
        private val TASK =
            Regex(pattern = "^\\s*(?:>\\s*)*(?:[-*+]|\\d+[.)])\\s+\\[([ xX])](?=\\s|$)")
    }
}
