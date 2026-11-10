package app.notekea.chat.eval

/**
 * A question of the answer evaluation set on a sample [vault]: the [notes] that answer it and its [facts], each a list
 * of the ways one fact can be written (any one counts). A question with no facts is a trap: the notes do not answer
 * it, and the right answer is to say so.
 */
data class AnswerQuestion(
    val vault: String,
    val question: String,
    val notes: List<String>,
    val facts: List<List<String>>
) {
    val isTrap: Boolean get() = facts.isEmpty()

    /** Asked in Spanish: its answer must be too. */
    val isSpanish: Boolean get() = question.any {
        it in SPANISH_MARKS
    }

    private companion object {
        const val SPANISH_MARKS = "¿áéíóúñ"
    }
}
