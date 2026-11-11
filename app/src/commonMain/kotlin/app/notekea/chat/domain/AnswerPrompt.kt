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

    // A short reply that only says the excerpts do not tell, in words of its own: "No se menciona en los extractos…".
    private val SAYS_NOT_FOUND = Regex(
        pattern = "^\\W*(no se (menciona|encuentra|encontr\\w*|dice|indica|especifica)|no (hay|encontr\\w*|aparece)|" +
            "(the )?(excerpts?|passages?|notes?) (do|does) not|there is no|i (could not|couldn't|cannot|can't) " +
            "find|(it is )?not (mentioned|found|stated)|no se puede (determinar|saber|decir)|" +
            "i cannot (tell|determine|say))\\b[^.]*\\b(extractos?|fragmentos?|excerpts?|passages?|notas|notes?|" +
            "mention\\w*|menciona\\w*)",
        option = RegexOption.IGNORE_CASE
    )

    private const val SHORT_REPLY = 200

    /** Whether [text], an answer written from excerpts, says they do not answer the question. */
    fun isNotFound(text: String): Boolean {
        val trimmed = text.trim()

        return trimmed.isEmpty() ||
            NOT_FOUND in trimmed ||
            (trimmed.length <= SHORT_REPLY && SAYS_NOT_FOUND.containsMatchIn(input = trimmed))
    }

    private const val INSTRUCTIONS =
        "Answer in one to three sentences using only the excerpts, in the language of the question. Cite each " +
            "sentence with the number of its excerpt, like [1]. If the excerpts do not contain the answer, reply " +
            "exactly $NOT_FOUND. Do not explain your reasoning. Speak to the person as you (\"you wrote\"). To add " +
            "or count numbers from the excerpts, write " +
            "each number and the result (4500 + 300 = 4800)."

    // "Escribe un poema sobre mis recetas": written from the excerpts, not reported (the chat probe, 2026-11-11).
    private val CREATIVE = Regex(
        pattern = "\\b(poem|poema|poesía|poesia|haiku|story|cuento|historia corta|joke|chiste|song|canción|cancion|" +
            "verse|verso|rima|rhyme|limerick)\\b",
        option = RegexOption.IGNORE_CASE
    )

    private const val CREATIVE_INSTRUCTIONS =
        "Write what the person asks for (a poem, a story, a joke) from the excerpts of their notes: short (a joke " +
            "in two to four lines, a poem in at most eight), using " +
            "what they say, in the language of the request. No citation numbers. The notes need not hold a poem or " +
            "a joke: write one about what they say. Only if the excerpts have nothing on the subject asked, " +
            "reply exactly $NOT_FOUND."

    fun messages(
        question: String,
        excerpts: List<String>
    ): List<ChatMessage> {
        val numbered = excerpts
            .mapIndexed { index, text -> "[${index + 1}] $text" }
            .joinToString(separator = "\n\n")

        val isCreative = CREATIVE.containsMatchIn(input = question)

        return listOf(
            ChatMessage(
                role = ChatRole.SYSTEM,
                text = if (isCreative) CREATIVE_INSTRUCTIONS else INSTRUCTIONS
            ),
            ChatMessage(
                role = ChatRole.USER,
                text = "Excerpts:\n$numbered\n\nQuestion: $question"
            )
        )
    }
}
