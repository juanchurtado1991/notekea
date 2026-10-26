package app.notekea.notes.domain

import app.notekea.core.domain.markdown.MarkdownSyntax
import app.notekea.core.domain.StringPatterns.NEW_LINE_CHAR

/**
 * The tasks of a note's text (`ROADMAP.md` 1.7), as Obsidian reads them: a list item (`-`, `*`, `+` or `1.`), also
 * inside quotes and callouts, that starts with `[ ]`, `[x]` or `[X]` followed by a space or the end of the line. Lines in
 * fenced code blocks are not tasks.
 */
class TaskFinder {
    fun tasks(
        text: String
    ): List<TaskMarker> {
        val tasks = mutableListOf<TaskMarker>()
        var fence: Char? = null
        var lineStart = 0
        text.split(NEW_LINE_CHAR).forEach { line ->
            val marker = MarkdownSyntax.FENCE_LINE.find(input = line)?.groupValues?.get(index = 1)?.first()
            when {
                fence != null -> if (marker == fence) fence = null

                marker != null -> fence = marker

                else -> TASK.find(input = line)?.groups?.get(index = 1)?.let { state ->
                    tasks +=
                        TaskMarker(
                            stateIndex = lineStart + state.range.first,
                            isDone =
                            state.value.single() != MarkdownSyntax.TASK_OPEN_STATE
                        )
                }
            }
            lineStart += line.length + 1
        }
        return tasks
    }

    /** The task on the line that holds [offset] (the cursor), or null when that line is not a task. */
    fun taskAt(
        text: String,
        offset: Int
    ): TaskMarker? {
        val lineStart = text.lastIndexOf(char = NEW_LINE_CHAR, startIndex = offset - 1) + 1
        val lineEnd = text.indexOf(char = NEW_LINE_CHAR, startIndex = offset).takeIf { it >= 0 } ?: text.length
        return tasks(text = text).firstOrNull { it.stateIndex in lineStart until lineEnd }
    }

    companion object {
        private val TASK = Regex(pattern = "^\\s*(?:>\\s*)*(?:[-*+]|\\d+[.)])\\s+\\[([ xX])](?=\\s|$)")
    }
}
