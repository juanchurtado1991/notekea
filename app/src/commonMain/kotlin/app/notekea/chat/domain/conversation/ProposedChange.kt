package app.notekea.chat.domain.conversation

import app.notekea.chat.domain.plan.ActionKind
import app.notekea.core.notes.domain.RelativePath

/**
 * One change of a proposal as the conversation keeps it: its [kind], the note's [title] or the [notes] it acts on,
 * and the [start] of the text it writes.
 */
data class ProposedChange(
    val kind: ActionKind,
    val notes: List<RelativePath>,
    val start: String,
    val title: String?
) {
    companion object {
        /** How much of a written text is kept: enough to know later what it was. */
        const val START_CHARS = 280
    }
}
