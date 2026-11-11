package app.notekea.chat.domain

/** Where an answer stands while it is prepared, as the chat shows it, step by step. */
sealed interface AnswerStep {
    /** The notes are searched for the question. */
    data object Searching : AnswerStep

    /** The [notes] passages found are read to answer it. */
    data class Reading(val notes: Int) : AnswerStep

    /** The answer so far, as the model writes it from the passages [read]: `[n]` cites `read[n - 1]`. */
    data class Writing(
        val text: String,
        val read: List<AnswerSource>
    ) : AnswerStep

    /** Deep answer (3.6): [drafts] answers are written at once, to vote on. */
    data class Drafting(val drafts: Int) : AnswerStep

    /** A request the chat can do for them, offered beside a help answer: "create a note called …". */
    data class Offered(val request: String) : AnswerStep

    data class Done(val answer: Answer) : AnswerStep

    /** The notes do not answer it: nothing close enough was found, or the model said so. */
    data object NotFound : AnswerStep

    data class Failed(val error: ChatError) : AnswerStep
}
