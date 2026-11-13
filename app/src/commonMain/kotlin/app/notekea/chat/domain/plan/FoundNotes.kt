package app.notekea.chat.domain.plan

/** A table the chat shows: the [columns] and, per note (and line, for a task, when [isTasks]), its [rows]. */
data class FoundNotes(
    val columns: List<String> = emptyList(),
    val rows: List<FoundRow> = emptyList(),
    val query: String? = null,
    val isTasks: Boolean = false,
    // The tasks' period ("overdue"), so an answer from them knows what they are.
    val period: String? = null
)
