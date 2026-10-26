package app.notekea.notes.domain

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
        text.split('\n').forEach { line ->
            val marker = FENCE.find(input = line)?.groupValues?.get(index = 1)?.first()
            when {
                fence != null -> if (marker == fence) fence = null

                marker != null -> fence = marker

                else -> TASK.find(input = line)?.groups?.get(index = 1)?.let { state ->
                    tasks += TaskMarker(stateIndex = lineStart + state.range.first, isDone = state.value != " ")
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
        val lineStart = text.lastIndexOf(char = '\n', startIndex = offset - 1) + 1
        val lineEnd = text.indexOf(char = '\n', startIndex = offset).takeIf { it >= 0 } ?: text.length
        return tasks(text = text).firstOrNull { it.stateIndex in lineStart until lineEnd }
    }

    companion object {
        private val FENCE = Regex(pattern = "^\\s{0,3}(`{3,}|~{3,})")
        private val TASK = Regex(pattern = "^\\s*(?:>\\s*)*(?:[-*+]|\\d+[.)])\\s+\\[([ xX])](?=\\s|$)")
    }
}
