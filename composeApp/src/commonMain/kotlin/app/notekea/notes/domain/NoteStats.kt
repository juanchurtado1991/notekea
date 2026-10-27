package app.notekea.notes.domain

/**
 * What a note holds, for the side panel's Note info: its [words], the [links] it makes to other notes, its distinct
 * [tags] (written in the text or listed in its properties), and its [tasks], [doneTasks] of them checked.
 */
data class NoteStats(
    val words: Int,
    val links: Int,
    val tags: Int,
    val tasks: Int,
    val doneTasks: Int
) {
    /** Minutes to read it at [WORDS_PER_MINUTE], rounded up: at least one minute for any text, none for an empty note. */
    val readingMinutes: Int
        get() = (words + WORDS_PER_MINUTE - 1) / WORDS_PER_MINUTE

    companion object {
        const val WORDS_PER_MINUTE = 200
    }
}
