package app.notekea.notes.domain

/**
 * One change to a note's text: [start] until [end] is replaced by [replacement], then [selectionStart] until
 * [selectionEnd] (in the new text) is selected. One edit, so a toolbar button is undone in one step.
 */
data class TextEdit(
    val start: Int,
    val end: Int,
    val replacement: String,
    val selectionStart: Int,
    val selectionEnd: Int
)
