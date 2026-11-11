package app.notekea.chat.domain

import app.notekea.core.notes.domain.RelativePath

/** Why a plan cannot be done as asked, said in the chat instead of guessing. */
sealed interface ActionProblem {
    data class UnknownNote(val name: String) : ActionProblem

    data class UnknownFolder(val name: String) : ActionProblem

    data class Missing(
        val kind: ActionKind,
        val field: ActionField
    ) : ActionProblem

    data class TooManyNotes(val count: Int) : ActionProblem

    /** A note the plan would create is already there. */
    data class Exists(val path: RelativePath) : ActionProblem

    /** No open task of the note reads like [text]. */
    data class UnknownTask(val text: String) : ActionProblem

    /** The request in plain words could not be turned into a query of the vault's properties (3.8). */
    data class UnknownQuery(val property: String?) : ActionProblem

    /** A pasted text longer than the notes it can become ([words] in it). */
    data class TextTooLong(val words: Int) : ActionProblem

    /** The model or the notes failed while the step's text was written. */
    data class Failed(val error: ChatError) : ActionProblem

    /** Something the chat never does; [reason] as the model wrote it. */
    data class Refused(val reason: String) : ActionProblem

    /** Agent mode ran out of moves or time, or its model failed, before it could propose or finish (4.7). */
    data object AgentStopped : ActionProblem

    /** No study card could be made from the note: the note backs none the model proposed (4.6). */
    data object NoCards : ActionProblem

    /** A web page could not be clipped (4.4), for [reason]; no note was written. */
    data class PageFailed(val reason: PageProblem) : ActionProblem
}
