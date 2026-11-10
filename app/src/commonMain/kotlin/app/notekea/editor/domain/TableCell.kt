package app.notekea.editor.domain

/** One cell of a [TableText]: [start] until [end] is its text in the note, without the spaces around it. */
data class TableCell(
    val row: Int,
    val column: Int,
    val start: Int,
    val end: Int
)
