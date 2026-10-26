package app.notekea.notes.domain

import app.notekea.core.domain.markdown.MarkdownSyntax

/**
 * A Markdown task (`- [ ]` / `- [x]`) found in a note's text: [stateIndex] is where the character between the brackets
 * is, and [isDone] whether it is checked (`x` or `X`).
 */
data class TaskMarker(
    val stateIndex: Int,
    val isDone: Boolean
) {
    /** The character that toggles the task: `x` checks it, a space unchecks it. */
    val toggledState: Char get() = if (isDone) MarkdownSyntax.TASK_OPEN_STATE else MarkdownSyntax.TASK_DONE_STATE
}
