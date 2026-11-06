package app.notekea.search.eval

/**
 * A question of the evaluation set: it is answered when one of [notes] of [vault] is found. [heading] is where the answer
 * is written; recall counts the note alone.
 */
data class RetrievalQuestion(
    val vault: String,
    val question: String,
    val notes: List<String>,
    val heading: String? = null
)
