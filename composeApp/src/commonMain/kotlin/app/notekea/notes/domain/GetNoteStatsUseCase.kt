package app.notekea.notes.domain

import app.notekea.core.domain.StringPatterns

/**
 * Counts what a note's [NoteStats] say from its body: words are runs of text holding a letter or a digit (Markdown's
 * own marks, like `#`, `-`, `>` or a checked task's `[x]`, are not words), links are those [linkFinder] finds outside code, tags are the text's
 * `#tags` and the [propertyTags] of its properties, each counted once, and tasks are its checklist items.
 */
class GetNoteStatsUseCase(
    private val linkFinder: LinkFinder
) {
    private val textTagFinder = TextTagFinder()
    private val taskFinder = TaskFinder()

    operator fun invoke(
        body: String,
        propertyTags: List<String>
    ): NoteStats {
        val tasks = taskFinder.tasks(text = body)
        val tags = textTagFinder.tags(lines = body.lineSequence()) + propertyTags
        return NoteStats(
            words = body.split(StringPatterns.WHITESPACE).count { word ->
                word !in CHECKED_TASK_MARKS && word.any { it.isLetterOrDigit() }
            },
            links = linkFinder.find(markdown = body).size,
            tags = tags.map { it.lowercase() }.distinct().size,
            tasks = tasks.size,
            doneTasks = tasks.count { it.isDone }
        )
    }

    private companion object {
        val CHECKED_TASK_MARKS = setOf("[x]", "[X]")
    }
}
