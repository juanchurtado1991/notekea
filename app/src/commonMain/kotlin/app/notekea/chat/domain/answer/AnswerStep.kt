package app.notekea.chat.domain.answer

import app.notekea.chat.domain.ChatError

/** Where an answer stands while it is prepared, as the chat shows it, step by step. */
sealed interface AnswerStep {
    /** The answer so far, as the model writes it from the passages [read]: `[n]` cites `read[n - 1]`. */
    data class Writing(
        val text: String,
        val read: List<AnswerSource>
    ) : AnswerStep

    /** A request the chat can do for them, offered beside a help answer: "create a note called …". */
    data class Offered(val request: String) : AnswerStep

    data class Done(val answer: Answer) : AnswerStep

    data class Failed(val error: ChatError) : AnswerStep
}
