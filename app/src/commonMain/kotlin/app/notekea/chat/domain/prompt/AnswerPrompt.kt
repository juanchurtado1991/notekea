package app.notekea.chat.domain.prompt

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
        pattern = "^\\W*(no se (menciona|encuentra|encontr\\w*|dice|indica|especifica)|" +
            "(the )?(excerpts?|passages?|notes?) (do|does) not|i (could not|couldn't|cannot|can't) " +
            "find|(it is )?not (mentioned|found|stated)|no se puede (determinar|saber|decir)|" +
            "i cannot (tell|determine|say))\\b[^.]*\\b(extractos?|fragmentos?|excerpts?|passages?|notas|notes?|" +
            "mention\\w*|menciona\\w*|lista|list|informaci[oó]n|information)",
        option = RegexOption.IGNORE_CASE
    )

    // "No hay…", "There is no…" also begin real answers ("No hay azúcar en la lista de ingredientes [1]."): they
    // say not found only of the notes themselves, and never with a citation (the code review of 2026-11-13).
    private val SAYS_NONE = Regex(
        pattern = "^\\W*(no (hay|encontr\\w*|aparece)|there is no|no \\w+( \\w+){0,3} (was|were|is|are) " +
            "(found|mentioned|listed))\\b[^.]*\\b(extractos?|fragmentos?|excerpts?|passages?|notas|notes?)",
        option = RegexOption.IGNORE_CASE
    )

    private val CITED = Regex(pattern = "\\[\\d+]")

    private const val SHORT_REPLY = 320

    /** Whether [text], an answer written from excerpts, says they do not answer the question. */
    fun isNotFound(text: String): Boolean {
        val trimmed = text.trim()

        return trimmed.isEmpty() ||
            NOT_FOUND in trimmed ||
            (trimmed.length <= SHORT_REPLY && saysSo(text = trimmed))
    }

    private fun saysSo(text: String): Boolean {
        val isUncited = !CITED.containsMatchIn(input = text)

        return SAYS_NOT_FOUND.containsMatchIn(input = text) ||
            (isUncited && SAYS_NONE.containsMatchIn(input = text))
    }

    private const val INSTRUCTIONS =
        "Answer briefly using only the excerpts, in the language of the question: one to three sentences, or a " +
            "short list when the answer is steps or several things. ${ReplyFormat.MARKDOWN} Cite each " +
            "sentence or item with the number of its excerpt, like [1]. If the excerpts do not contain the answer, reply " +
            "exactly $NOT_FOUND. A question of what the person learned or thinks about something is answered with what the " +
            "excerpts say about it. Only when two excerpts give different values for the same thing (two counts of " +
            "one book list), say so and give each with its number; excerpts about other things do not disagree, " +
            "leave them out. Say how many there are only when asked how many. Do not explain your reasoning. Speak to the person as you (\"you wrote\"). When the question asks for a total " +
            "or a count, work it out yourself from the excerpts' numbers, even when no excerpt says it: when you add " +
            "two or more, write each number and the result (a + b + c = total)."

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

    /** Whether [text] asks for something written from the notes, a poem, a story or a joke, rather than a fact. */
    fun isCreative(text: String): Boolean = CREATIVE.containsMatchIn(input = text)

    fun messages(
        question: String,
        excerpts: List<String>
    ): List<ChatMessage> {
        val numbered = excerpts
            .mapIndexed { index, text -> "[${index + 1}] $text" }
            .joinToString(separator = "\n\n")

        val isCreative = isCreative(text = question)

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
