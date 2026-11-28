package app.notekea.chat.domain.prompt

/**
 * The not found reply of an answer from the notes: [NOT_FOUND], the word the answer's instructions ask for when the
 * notes do not hold it, or a short reply that says so in words of its own ([isNotFound]).
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

    // "The notes do not contain the cost of a single ride…": the notes are its subject, saying what they lack, with
    // none of the words above after it (the web's evaluation, 2026-11-16, where the web was then never offered).
    private val NOTES_LACK = Regex(
        pattern = "^\\W*(the |your )?(excerpts?|passages?|notes?) (do|does) not (contain|hold|include|have|say|" +
            "mention|give|state|tell|list)\\b|^\\W*(las |tus )?(notas|extractos?) no (contienen|incluyen|dicen|" +
            "mencionan|tienen|indican|dan)\\b",
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
            (isUncited && SAYS_NONE.containsMatchIn(input = text)) ||
            (isUncited && NOTES_LACK.containsMatchIn(input = text))
    }
}
