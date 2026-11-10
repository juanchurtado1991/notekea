package app.notekea.chat.domain

/** A table the chat shows: the [columns] and, per note (and line, for a task), its [rows]. */
data class FoundNotes(
    val columns: List<String> = emptyList(),
    val rows: List<FoundRow> = emptyList(),
    val query: String? = null
)
