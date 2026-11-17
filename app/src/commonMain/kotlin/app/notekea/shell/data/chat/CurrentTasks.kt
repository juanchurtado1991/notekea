package app.notekea.shell.data.chat

import app.notekea.core.domain.StringPatterns
import app.notekea.core.notes.domain.NoteDocument
import app.notekea.core.notes.domain.tasks.NoteTask
import app.notekea.core.notes.domain.tasks.TaskFinder

/**
 * A note's tasks read from its text as it is now, where the plan's index is a snapshot from before it: a task the
 * plan added could not be ticked, and of two identical lines both ticks went to the first (T15). Indexes are into the
 * note's body, as the Tasks tab's check reads them.
 */
internal object CurrentTasks {
    private val finder = TaskFinder()

    fun of(text: String): List<NoteTask> {
        val body = NoteDocument.fromText(text = text).body

        return finder.tasks(text = body).map { marker ->
            val end = body
                .indexOf(
                    char = StringPatterns.NEW_LINE_CHAR,
                    startIndex = marker.stateIndex
                )
                .takeIf {
                    it >= 0
                } ?: body.length

            NoteTask(
                stateIndex = marker.stateIndex,
                endIndex = end,
                text = body
                    .substring(
                        startIndex = (marker.stateIndex + 2).coerceAtMost(maximumValue = end),
                        endIndex = end
                    )
                    .trim(),
                isDone = marker.isDone
            )
        }
    }
}
