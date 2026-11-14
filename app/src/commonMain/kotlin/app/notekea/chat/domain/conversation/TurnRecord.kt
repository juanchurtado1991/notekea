package app.notekea.chat.domain.conversation

import app.notekea.chat.domain.ChatTurn
import app.notekea.chat.domain.plan.ActionKind

/**
 * What the chat did in a turn, as the model reads it later: what it looked up, what it proposed and how that ended,
 * and whether a note's text came from the person's notes or from the model alone. Without it a "did you make that
 * up?" was answered from the vault, citing the made-up note (the developer's smoke of 2026-11-14).
 */
object TurnRecord {
    /** Starts every record, and tells the model the turn was its own work. */
    const val MARK =
        "(A record of what I, the assistant, did for this request; not an answer from the notes.)"

    private val WRITTEN = setOf(ProposalOutcome.DONE, ProposalOutcome.PROPOSED)

    fun of(turn: ChatTurn): String? {
        val calls = turn.calls
        val proposal = turn.proposal
        val isEmpty = calls.isEmpty() && proposal == null
        if (isEmpty) return null

        val read = calls
            .map { "${it.name} ${it.target}" }
            .distinct()

        val looked =
            if (read.isEmpty()) "I looked nothing up." else "I looked up: ${read.joinToString()}."

        val status = when (proposal?.outcome) {
            null -> "I proposed no change."
            ProposalOutcome.DONE -> "The person confirmed it and it was done."
            ProposalOutcome.PROPOSED -> "I proposed it on a card; the person has not confirmed it."
            ProposalOutcome.FAILED -> "It was not done: ${proposal.problem}."
            ProposalOutcome.CANCELLED -> "The person cancelled it."
            ProposalOutcome.UNDONE -> "It was done, then the person undid it."
        }

        // What was never proposed or done wrote nothing: its steps would read as notes that exist.
        val isWritten = proposal?.outcome in WRITTEN

        val written = proposal?.changes.orEmpty()

        val changes = if (isWritten) {
            written.joinToString(separator = "") { "\n- ${changeOf(change = it)}" }
        } else {
            " No note was written or changed."
        }

        return "$MARK\n$looked $status$changes"
    }

    private fun changeOf(change: ProposedChange): String {
        val name = change.title ?: change.notes.joinToString { it.nameWithoutExtension }

        val origin = when (change.kind) {
            ActionKind.CREATE_NOTE ->
                " Its text I wrote myself from the request alone, without reading any note or document: it is " +
                    "my own example, not the person's data."

            ActionKind.WRITE_FROM_NOTES ->
                " I read ${change.notes.joinToString { it.name }} and wrote its text from what they say: it " +
                    "is taken from them, not made up."

            else -> ""
        }

        val text = change
            .start
            .takeIf { it.isNotBlank() }
            ?.let { " It starts: $it" }
            .orEmpty()

        return "${change.kind.written} \"$name\".$origin$text"
    }
}
