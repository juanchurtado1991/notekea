package app.notekea.chat.domain

/** Where an answer stands while it is prepared, as the chat shows it, step by step. */
sealed interface AnswerStep {
    /** The notes are searched for the question. */
    data object Searching : AnswerStep

    /** The [notes] passages found are read to answer it. */
    data class Reading(val notes: Int) : AnswerStep

    /** The answer so far, as the model writes it; its citations are only checked at the end. */
    data class Writing(val text: String) : AnswerStep

    data class Done(val answer: Answer) : AnswerStep

    /** The notes do not answer it: nothing close enough was found, or the model said so. */
    data object NotFound : AnswerStep

    data class Failed(val error: ChatError) : AnswerStep
}
