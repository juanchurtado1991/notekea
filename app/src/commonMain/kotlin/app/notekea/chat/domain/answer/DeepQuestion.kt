package app.notekea.chat.domain.answer

/**
 * Whether Auto answers a question with Deep thinking (3.6): when it asks to reason over the notes (compare, explain,
 * why, sum up), where three drafts help; a question of one fact is answered at once, 2.7 times faster.
 */
object DeepQuestion {
    private val NOT_A_LETTER = Regex(pattern = "[^\\p{L}\\p{N}]+")

    private val REASONING = listOf(
        "compare", "comparing", "difference", "differences", "versus", "vs", "why", "explain", "summarize",
        "summarise", "summary", "pros and cons", "relationship", "compara", "comparar", "diferencia",
        "diferencias", "por qué", "por que", "explica", "explicame", "explícame", "resume", "resumen", "resumí",
        "ventajas", "relación"
    )

    fun isAsked(question: String): Boolean {
        val words = " " + question
            .lowercase()
            .replace(NOT_A_LETTER, " ")
            .trim() + " "

        return REASONING.any {
            " $it " in words
        }
    }
}
