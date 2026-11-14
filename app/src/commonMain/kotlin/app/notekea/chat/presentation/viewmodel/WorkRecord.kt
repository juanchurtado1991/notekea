package app.notekea.chat.presentation.viewmodel

import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.ActionStep
import app.notekea.chat.presentation.model.ActionStatus
import app.notekea.chat.presentation.model.ChatEntry

/**
 * What the chat did for a request, as the model reads it later in the chat's memory: what it read, what it proposed
 * and how that ended, and whether a note's text came from the person's notes or from the model alone. Without it a
 * "did you make that up?" was answered from the vault, citing the made-up note (the developer's smoke of 2026-11-14).
 */
internal object WorkRecord {
    /** Starts every record, and tells the model the turn was its own work. */
    const val MARK =
        "(A record of what I, the assistant, did for this request; not an answer from the notes.)"

    private const val TEXT_CHARS = 280

    fun of(entry: ChatEntry): String? {
        val action = entry.action ?: return null

        val isEmpty =
            entry.moves.isEmpty() &&
                action.steps.isEmpty() &&
                action.problem == null

        if (isEmpty) return null

        val read = entry
            .moves
            .map { "${it.move} ${it.target}" }
            .distinct()

        val looked =
            if (read.isEmpty()) "I looked nothing up." else "I looked up: ${read.joinToString()}."

        val status = when (action.status) {
            ActionStatus.DONE -> "The person confirmed it and it was done."
            ActionStatus.PROPOSED -> "I proposed it on a card; the person has not confirmed it."
            ActionStatus.FAILED -> "It was not done: ${action.problem}."
            else -> "It ended as ${action.status.name.lowercase()}."
        }

        // What was never proposed or done wrote nothing: its steps would read as notes that exist.
        val isWritten = action.status == ActionStatus.DONE || action.status == ActionStatus.PROPOSED

        val steps = if (isWritten) {
            action.steps.joinToString(separator = "") { "\n- ${stepOf(step = it)}" }
        } else {
            " No note was written or changed."
        }

        return "$MARK\n$looked $status$steps"
    }

    private fun stepOf(step: ActionStep): String {
        val name = step.title ?: step.notes.joinToString { it.nameWithoutExtension }

        val origin = when (step.kind) {
            ActionKind.CREATE_NOTE ->
                " Its text I wrote myself from the request alone, without reading any note or document: it is " +
                    "my own example, not the person's data."

            ActionKind.WRITE_FROM_NOTES ->
                " I read ${step.notes.joinToString { it.name }} and wrote its text from what they say: it " +
                    "is taken from them, not made up."

            else -> ""
        }

        val text = step
            .content
            .firstOrNull()
            ?.text
            ?.take(n = TEXT_CHARS)
            ?.replace(
                oldChar = '\n',
                newChar = ' '
            )
            ?.let { " It starts: $it" }
            .orEmpty()

        return "${step.kind.written} \"$name\".$origin$text"
    }
}
