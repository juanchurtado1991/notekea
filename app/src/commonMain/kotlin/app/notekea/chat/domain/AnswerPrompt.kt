package app.notekea.chat.domain

import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.ChatRole

/**
 * What the model is given to answer from the notes, as measured in `docs/spikes/2026-11-08-answer-quality.md` and the
 * model tournament: one call over the reranked excerpts, numbered, a short answer in the question's language citing
 * them, and [NOT_FOUND] when they do not answer it.
 */
object AnswerPrompt {
    const val NOT_FOUND = "NOT_FOUND"

    private const val INSTRUCTIONS =
        "Answer in one to three sentences using only the excerpts, in the language of the question. Cite each " +
            "sentence with the number of its excerpt, like [1]. If the excerpts do not contain the answer, reply " +
            "exactly $NOT_FOUND. Do not explain your reasoning."

    fun messages(
        question: String,
        excerpts: List<String>
    ): List<ChatMessage> {
        val numbered = excerpts
            .mapIndexed { index, text -> "[${index + 1}] $text" }
            .joinToString(separator = "\n\n")

        return listOf(
            ChatMessage(
                role = ChatRole.SYSTEM,
                text = INSTRUCTIONS
            ),
            ChatMessage(
                role = ChatRole.USER,
                text = "Excerpts:\n$numbered\n\nQuestion: $question"
            )
        )
    }
}
