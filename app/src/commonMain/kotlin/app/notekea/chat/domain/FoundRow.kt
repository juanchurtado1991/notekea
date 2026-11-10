package app.notekea.chat.domain

import app.notekea.core.notes.domain.RelativePath

/** One row: the [note] it cites, the [line] of a task in it, and the [cells] of the columns. */
data class FoundRow(
    val note: RelativePath,
    val cells: List<String> = emptyList(),
    val line: Int? = null
)
